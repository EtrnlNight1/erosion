package co.bracesoftware.erosion;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/** Reloadable server settings, independent of Minecraft so validation can be tested. */
public final class ErosionPerformanceConfig {
    private record Setting(String key, int defaultValue, int min, int max, String description) {}
    private static final Map<String, Setting> SETTINGS = new LinkedHashMap<>();
    private static volatile Map<String, Integer> values;
    static {
        define("alterations_per_tick", 15, 1, 1000, "Base budget per alteration queue; priority uses +1, delayed processing uses multipliers.");
        define("event_delay_ticks", 12, 1, 1200, "Delay before handling new events. Already scheduled work keeps its original delay.");
        define("delayed_processing_interval_minutes", 2, 1, 120, "How often the delayed alteration queue is revisited.");
        define("cluster_count_per_pass", 5, 0, 20, "Areas scanned per Overworld player per pass. Zero disables background cluster scanning.");
        define("cluster_interval_ticks", 1, 1, 1200, "Ticks between cluster passes. 20 ticks is approximately one second at full TPS.");
        define("cluster_size", 8, 2, 16, "Scan diameter in blocks. Increasing this sharply increases work.");
        define("cluster_min_distance_blocks", 24, 0, 512, "Minimum horizontal distance from the player. Maximum follows simulation distance.");
        define("flowing_fluid_check_percent", 30, 0, 100, "Chance used by the existing flowing-fluid checks; also affects alteration behavior.");
        define("retrogen_attempts_per_chunk", 10, 1, 100, "Surface positions tried during each mineral placement attempt.");
        define("cluster_loaded_chunks_only", 0, 0, 1, "1 restricts background scanning to chunks already loaded; 0 preserves original behavior.");
        values = defaults();
    }
    private ErosionPerformanceConfig() {}
    private static void define(String key, int value, int min, int max, String description) {
        SETTINGS.put(key, new Setting(key, value, min, max, description));
    }
    private static Map<String, Integer> defaults() {
        Map<String, Integer> result = new LinkedHashMap<>();
        SETTINGS.values().forEach(s -> result.put(s.key(), s.defaultValue()));
        return Map.copyOf(result);
    }
    public static int get(String key) {
        Integer value = values.get(key);
        if (value == null) throw new IllegalArgumentException("Unknown setting: " + key);
        return value;
    }
    public static boolean contains(String key) { return SETTINGS.containsKey(key); }
    private static int parse(Setting setting, String raw) {
        int value = Integer.parseInt(raw.trim());
        if (value < setting.min() || value > setting.max())
            throw new IllegalArgumentException(setting.key() + " must be between " + setting.min() + " and " + setting.max());
        return value;
    }
    public static synchronized void set(String key, String raw) {
        Setting setting = SETTINGS.get(key);
        if (setting == null) throw new IllegalArgumentException("Unknown setting: " + key);
        Map<String, Integer> next = new LinkedHashMap<>(values);
        next.put(key, parse(setting, raw));
        values = Map.copyOf(next);
    }
    /** Failed reads preserve the current settings and never overwrite the unreadable file. */
    public static synchronized void load(Path file, Consumer<String> log) {
        Properties properties = new Properties();
        boolean exists = Files.exists(file);
        if (exists) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                log.accept("Could not read " + file + "; keeping current settings: " + e.getMessage());
                return;
            }
        }
        Map<String, Integer> next = new LinkedHashMap<>();
        for (Setting setting : SETTINGS.values()) {
            int value = setting.defaultValue();
            String raw = properties.getProperty(setting.key());
            if (raw != null) {
                try { value = parse(setting, raw); }
                catch (IllegalArgumentException e) {
                    log.accept("Invalid " + setting.key() + "='" + raw + "'; using default " + value);
                }
            }
            next.put(setting.key(), value);
        }
        properties.stringPropertyNames().stream().filter(key -> !contains(key))
            .forEach(key -> log.accept("Unknown performance setting: " + key));
        values = Map.copyOf(next);
        // Existing files are left intact so user comments and invalid values remain available to inspect.
        if (!exists) save(file, log);
    }
    public static synchronized void save(Path file, Consumer<String> log) {
        StringBuilder text = new StringBuilder("# Erosion server performance settings\n# Edit, then run /erosion reload_config. Values below are original defaults.\n# Queue budgets are not a global time limit; heavy block updates can still cause lag.\n\n");
        for (Setting setting : SETTINGS.values()) {
            text.append("# ").append(setting.description()).append('\n');
            text.append("# Range: ").append(setting.min()).append("..").append(setting.max())
                .append("; default: ").append(setting.defaultValue()).append('\n');
            text.append(setting.key()).append('=').append(get(setting.key())).append("\n\n");
        }
        Path temp = null;
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            temp = Files.createTempFile(file.toAbsolutePath().getParent(), "erosion-settings-", ".tmp");
            Files.writeString(temp, text, StandardCharsets.UTF_8);
            try { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.accept("Could not save " + file + ": " + e.getMessage());
        } finally {
            if (temp != null) try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
        }
    }
    public static Map<String, Integer> entries() { return new LinkedHashMap<>(values); }
}
