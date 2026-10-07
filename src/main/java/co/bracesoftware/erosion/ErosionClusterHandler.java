package co.bracesoftware.erosion;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = Erosion.MODID)
public class ErosionClusterHandler
{
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event)
    {
        var server = ServerLifecycleHooks.getCurrentServer();
        if(server == null) return;
        if(server.getTickCount() % ErosionPerformanceConfig.get("cluster_interval_ticks") != 0) return;

        var overworld = server.overworld();
        
        int simDistanceChunks = server.getPlayerList().getSimulationDistance();
        int maxRadius = simDistanceChunks * ErosionConfig.CHUNK_SIZE;

        for(ServerPlayer player : overworld.players())
        {
            selectVein(overworld, player.blockPosition(), maxRadius);
        }
    }

    private static void selectVein(ServerLevel level, BlockPos playerPos, int maxRadius)
    {
        int minRadiusSq = ErosionPerformanceConfig.get("cluster_min_distance_blocks") * ErosionPerformanceConfig.get("cluster_min_distance_blocks");
        int maxRadiusSq = maxRadius * maxRadius;
        int clusterRadius = ErosionPerformanceConfig.get("cluster_size") / 2;
        int clusterRadiusSq = clusterRadius * clusterRadius;

        for(int c = 0; c < ErosionPerformanceConfig.get("cluster_count_per_pass"); c++)
        {
            int baseOffsetX = 0, baseOffsetZ = 0, distanceSq = 0;
            int attempts = 0;

            do {
                baseOffsetX = ErosionMod.RANDOM.nextInt(maxRadius * 2 + 1) - maxRadius;
                baseOffsetZ = ErosionMod.RANDOM.nextInt(maxRadius * 2 + 1) - maxRadius;
                distanceSq = baseOffsetX * baseOffsetX + baseOffsetZ * baseOffsetZ;
                attempts++;
            } while ((distanceSq < minRadiusSq || distanceSq > maxRadiusSq) && attempts < 15);

            if(attempts >= 15) continue;

            int baseOffsetY = ErosionMod.RANDOM.nextInt(ErosionConfig.CHUNK_SIZE * 2) - ErosionConfig.CHUNK_SIZE;
            var clusterCenter = playerPos.offset(baseOffsetX, baseOffsetY, baseOffsetZ);

            for(int x = -clusterRadius; x <= clusterRadius; x++)
            {
                for(int y = -clusterRadius; y <= clusterRadius; y++)
                {
                    for(int z = -clusterRadius; z <= clusterRadius; z++)
                    {
                        if(x * x + y * y + z * z <= clusterRadiusSq)
                        {
                            var targetPos = clusterCenter.offset(x, y, z);
                            if(ErosionPerformanceConfig.get("cluster_loaded_chunks_only") == 1
                                && !level.getChunkSource().hasChunk(targetPos.getX() >> 4, targetPos.getZ() >> 4)) continue;
                            for(var f : ErosionRetrogen.RETROGEN_FEATURES)
                            {
                                ErosionRetrogen.applyFeatureToChunk(level, targetPos, f);
                            }
                            ErosionCore.addCandidatePriority(level, targetPos);
                        }
                    }
                }
            }
        }
        return;
    }
}
