package co.bracesoftware.erosion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/** Run with Java 21; no Minecraft launch or third-party test library required. */
public final class ErosionPerformanceConfigTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("erosion-config-test-");
        Path file = dir.resolve("performance.properties");
        var warnings = new ArrayList<String>();
        try {
            ErosionPerformanceConfig.load(file, warnings::add);
            check(Files.exists(file), "Missing config should be generated");
            check(ErosionPerformanceConfig.get("cluster_count_per_pass") == 5, "Original cluster default");
            check(ErosionPerformanceConfig.get("alterations_per_tick") == 15, "Original alteration default");
            String input = "# My comments\ncluster_count_per_pass=1\ncluster_interval_ticks=20\n"
                + "cluster_size=999\nflowing_fluid_check_percent=oops\nevent_delay_ticks=0\nunknown_key=2\n";
            Files.writeString(file, input);
            ErosionPerformanceConfig.load(file, warnings::add);
            check(ErosionPerformanceConfig.get("cluster_count_per_pass") == 1, "Reload custom count");
            check(ErosionPerformanceConfig.get("cluster_interval_ticks") == 20, "Reload custom interval");
            check(ErosionPerformanceConfig.get("cluster_size") == 8, "Out-of-range falls back");
            check(ErosionPerformanceConfig.get("flowing_fluid_check_percent") == 30, "Malformed falls back");
            check(ErosionPerformanceConfig.get("event_delay_ticks") == 12, "Prevent zero tick interval");
            check(warnings.size() == 4, "Invalid and unknown values should be logged");
            check(Files.readString(file).equals(input), "Reload must preserve user comments");
            ErosionPerformanceConfig.set("cluster_count_per_pass", "0");
            check(ErosionPerformanceConfig.get("cluster_count_per_pass") == 0, "Allow disabling scan");
            try {
                ErosionPerformanceConfig.set("cluster_interval_ticks", "-1");
                throw new AssertionError("Invalid command accepted");
            } catch (IllegalArgumentException expected) {}
            check(ErosionPerformanceConfig.get("cluster_interval_ticks") == 20, "Invalid command must keep prior value");
            ErosionPerformanceConfig.save(file, warnings::add);
            ErosionPerformanceConfig.load(file, warnings::add);
            check(ErosionPerformanceConfig.get("cluster_count_per_pass") == 0, "Save/load round trip");
            Files.writeString(file, "cluster_count_per_pass=\\uZZZZ\n");
            ErosionPerformanceConfig.load(file, warnings::add);
            check(ErosionPerformanceConfig.get("cluster_count_per_pass") == 0, "Unreadable file keeps active values");
            check(Files.readString(file).contains("ZZZZ"), "Unreadable file must not be overwritten");
            System.out.println("All config validation, reload, persistence and recovery checks passed.");
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(dir);
        }
    }
}
