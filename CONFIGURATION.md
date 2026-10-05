# Adjustable server processing

This fork reads `erosion_config/performance.properties` inside the Minecraft instance or server directory. It creates a commented file on the first world/server start. Existing `aggressive_geochemical_alteration.sys_cfg` and `adaptive_data_cleaning.sys_cfg` files continue to work.

Edit the properties file, then run `/erosion reload_config` with operator permission. `/erosion view_config` displays active values. `/erosion set_config cluster_interval_ticks 20` changes and saves a setting immediately. Saving through the command regenerates the performance file's standard comments; ordinary reload preserves your comments. Watch the log for read/save failures.

| Setting | Default | Allowed range | Purpose |
| --- | ---: | --- | --- |
| alterations_per_tick | 15 | 1–1000 | Base alteration queue budget; priority uses +1 and delayed processing uses multipliers. This is not a global budget. |
| event_delay_ticks | 12 | 1–1200 | Delay before event work; pending scheduled work retains its original delay. |
| delayed_processing_interval_minutes | 2 | 1–120 | Delayed queue processing frequency. |
| cluster_count_per_pass | 5 | 0–20 | Scan areas per Overworld player; 0 stops background cluster scanning. |
| cluster_interval_ticks | 1 | 1–1200 | Time between scans, in server ticks. |
| cluster_size | 8 | 2–16 | Scan diameter; cost increases sharply with size. |
| cluster_min_distance_blocks | 24 | 0–512 | Minimum horizontal scan distance. Maximum still follows simulation distance. |
| flowing_fluid_check_percent | 30 | 0–100 | Existing fluid-check probability; changes gameplay as well as processing. |
| retrogen_attempts_per_chunk | 10 | 1–100 | Surface position attempts per mineral placement call. |
| cluster_loaded_chunks_only | 0 | 0–1 | Set to 1 to skip unloaded chunks in background cluster scans. |

All defaults preserve upstream behavior. Invalid numbers fall back to the individual original default and produce a log message. A malformed/unreadable properties file preserves current settings. Missing settings use defaults. These are server settings; separate multiplayer clients don't control the server's processing.

For a gentler starting point, try:

```properties
alterations_per_tick=6
cluster_count_per_pass=1
cluster_interval_ticks=20
cluster_size=8
cluster_loaded_chunks_only=1
```

This keeps background scanning enabled, but reduces its frequency significantly. Minerals and geological changes may appear more slowly. It does not fix every source of lag or add a strict time budget. Queue storage sizes, recipes, machine timings and debug constants are intentionally unchanged.

## Build and check

Use a Java 21 JDK and run `gradlew.bat build` on Windows or `./gradlew build` elsewhere. The new jar is `build/libs/Erosion-neoforge.jar`; the prebuilt jar committed at the repository root is still upstream's original and does not include these changes.

Run the standalone settings checks using Java 21:

```text
javac -d build/config-check src/main/java/co/bracesoftware/erosion/ErosionPerformanceConfig.java src/test/java/co/bracesoftware/erosion/ErosionPerformanceConfigTest.java
java -cp build/config-check co.bracesoftware.erosion.ErosionPerformanceConfigTest
```

Test the rebuilt jar in a copied instance and world before replacing the mod in your main instance. Compare Spark profiles with the same location and settings, then adjust one value at a time.
