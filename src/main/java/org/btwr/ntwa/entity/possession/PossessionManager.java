package org.btwr.ntwa.entity.possession;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.btwr.ntwa.data.ModDataAttachments;
import org.btwr.ntwa.data.PossessionData;
import org.btwr.ntwa.entity.possession.behavior.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PossessionManager {

    public static final Map<EntityType<?>, BiConsumer<LivingEntity, PossessionData>> BEHAVIORS = new HashMap<>();

    public static void registerBehaviors() {
        BEHAVIORS.put(EntityType.SHEEP, SheepPossessionBehavior::tick);
        BEHAVIORS.put(EntityType.SQUID, SquidPossessionBehavior::tick);
        BEHAVIORS.put(EntityType.WOLF, WolfPossessionBehavior::tick);
    }

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

    public static void onFullPossession(LivingEntity entity) {
        if (entity instanceof ChickenEntity chicken) {
            ChickenPossessionBehavior.onFullPossession(chicken);
        }

        if (entity instanceof VillagerEntity villager) {
            VillagerPossessionBehavior.onFullPossession(villager);
        }
    }

    public static boolean spreadPossession(LivingEntity source, double range, PossessionSource reason) {
        return trySpreadPossession(source.getWorld(), source.getBoundingBox().expand(range), source);
    }

    private static boolean trySpreadPossession(World world, Box box, LivingEntity exclude) {
        for (LivingEntity target : world.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e != exclude
        )) {
            var data = target.getAttached(ModDataAttachments.POSSESSABLE);

            if (data != null && !data.isPossessed()) {
                initiatePossession(target, data);
                return true;
            }
        }

        return false;
    }

    private static final double SOUL_URN_MISS_RANGE = 4.0;

    public static boolean onSoulUrnHit(LivingEntity target, PossessionSource reason) {
        var data = target.getAttached(ModDataAttachments.POSSESSABLE);
        if (data == null || data.isPossessed()) return false;

        initiatePossession(target, data);
        return true;
    }

    public static boolean onSoulUrnMiss(World world, Vec3d pos, PossessionSource reason) {
        return trySpreadPossession(world, Box.of(pos, SOUL_URN_MISS_RANGE * 2, SOUL_URN_MISS_RANGE * 2, SOUL_URN_MISS_RANGE * 2), null);
    }

    private static final double HOPPER_POSSESSION_RANGE = 16.0;

    public static boolean onHopperFilteringFailure(World world, BlockPos pos, PossessionSource reason) {
        return trySpreadPossessionAt(world, pos, HOPPER_POSSESSION_RANGE);
    }

    public static boolean onHopperExplosion(World world, BlockPos pos, PossessionSource reason) {
        return trySpreadPossessionAt(world, pos, HOPPER_POSSESSION_RANGE);
    }

    private static boolean trySpreadPossessionAt(World world, BlockPos pos, double range) {
        Vec3d center = pos.toCenterPos();
        return trySpreadPossession(world, Box.of(center, range * 2, range * 2, range * 2), null);
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
