package org.btwr.ntwa.entity.possession;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.btwr.ntwa.data.ModDataAttachments;
import org.btwr.ntwa.data.PossessionData;

import java.util.Map;
import java.util.function.BiConsumer;

public class PossessionManager {

    private static final Map<EntityType<?>, BiConsumer<LivingEntity, PossessionData>> BEHAVIORS = PossessionBehaviors.BEHAVIORS;

    public static PossessionSource<?> determineSource(LivingEntity entity) {
        var world = entity.getWorld();
        if (world.getDimensionEntry().matchesId(DimensionTypes.THE_NETHER_ID)) {
            return new PossessionSource.NetherExposure();
        }

        if (isNearNetherPortal(entity, 16)) {
            return new PossessionSource.NetherPortal();
        }

        return null;
    }

    public static void tickPossessed(LivingEntity entity, PossessionData data) {
        BiConsumer<LivingEntity, PossessionData> behavior = BEHAVIORS.get(entity.getType());
        if (behavior != null) {
            behavior.accept(entity, data);
        }
    }


    public static void initiatePossession(LivingEntity entity, PossessionData data) {
        data.setLevel(1);
        data.setTimer(data.getTimeToFullPossession(entity));
        tickPossessed(entity, data);
    }

    public static void onFullPossession(LivingEntity entity, PossessionData data) {
        // hook for later AI/buffs/visuals
    }

    public static boolean spreadPossession(
            LivingEntity source,
            double range,
            PossessionSource reason
    ) {
        var world = source.getWorld();

        for (LivingEntity target : world.getEntitiesByClass(
                LivingEntity.class,
                source.getBoundingBox().expand(range),
                e -> e != source
        )) {
            var data = target.getAttached(ModDataAttachments.POSSESSABLE);

            if (data != null && !data.isPossessed()) {
                initiatePossession(target, data);
                return true;
            }
        }

        return false;
    }

    private static final int PORTAL_CHECK_INTERVAL = 20; // once per second (20 ticks)

    public static boolean isNearNetherPortal(LivingEntity entity, int range) {
        if (entity.getWorld().isClient()) return false;

        PossessionData data = entity.getAttached(ModDataAttachments.POSSESSABLE);
        if (data == null) return false;

        // Only check every PORTAL_CHECK_INTERVAL ticks
        if (entity.age % PORTAL_CHECK_INTERVAL != 0) return false;

        World world = entity.getWorld();
        BlockPos center = entity.getBlockPos();

        int minX = center.getX() - range;
        int minY = center.getY() - range;
        int minZ = center.getZ() - range;

        int maxX = center.getX() + range;
        int maxY = center.getY() + range;
        int maxZ = center.getZ() + range;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) {
                        return true; // early exit on first portal found
                    }
                }
            }
        }

        return false;
    }

}
