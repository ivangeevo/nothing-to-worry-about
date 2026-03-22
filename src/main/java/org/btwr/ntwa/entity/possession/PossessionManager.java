package org.btwr.ntwa.entity.possession;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.btwr.ntwa.data.ModDataAttachments;
import org.btwr.ntwa.data.PossessionData;

import java.util.Map;
import java.util.function.BiConsumer;

public class PossessionManager {

    private static final Map<EntityType<?>, BiConsumer<LivingEntity, PossessionData>> BEHAVIORS = EntityTickPossessionBehaviors.TICK_BEHAVIORS;

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
        if (entity instanceof ChickenEntity chicken) {
            var world = chicken.getWorld();
            var random = chicken.getRandom();

            double x = chicken.getX();
            double y = chicken.getY();
            double z = chicken.getZ();

            if (!world.isClient()) {
                // --- Sounds (server-side so they sync) ---
                chicken.playSound(
                        SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                        1.0F,
                        (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F
                );

                chicken.playSound(SoundEvents.ENTITY_CHICKEN_HURT, 2.0F, random.nextFloat() * 0.4F + 1.2F);
            }

            // --- Particles (must be server -> send to clients) ---
            if (world instanceof ServerWorld serverWorld) {

                // light "blood mist" (reddust equivalent)
                for (int i = 0; i < 10; i++) {
                    serverWorld.spawnParticles(
                            DustParticleEffect.DEFAULT,
                            x + random.nextDouble(),
                            y + 1.0D + random.nextDouble(),
                            z + random.nextDouble(),
                            1,
                            0, 0, 0,
                            0
                    );
                }

                // drip lava
                for (int i = 0; i < 10; i++) {
                    serverWorld.spawnParticles(
                            ParticleTypes.DRIPPING_LAVA,
                            x - 0.5D + random.nextDouble(),
                            y + random.nextDouble() * 0.5,
                            z - 0.5D + random.nextDouble(),
                            1,
                            0, 0, 0,
                            0
                    );
                }

                // main explosion spray (redstone/item crack equivalent)
                for (int i = 0; i < 300; i++) {
                    serverWorld.spawnParticles(
                            new ItemStackParticleEffect(ParticleTypes.ITEM, Items.REDSTONE.getDefaultStack()),
                            x + random.nextDouble() - 0.5D,
                            y - 1.0D,
                            z + random.nextDouble() - 0.5D,
                            1,
                            (random.nextDouble() - 0.5D) * 0.5D,
                            0.2D + random.nextDouble() * 0.6D,
                            (random.nextDouble() - 0.5D) * 0.5D,
                            0
                    );
                }

                // bone fragments
                for (int i = 0; i < 25; i++) {
                    serverWorld.spawnParticles(
                            new ItemStackParticleEffect(ParticleTypes.ITEM, Items.BONE.getDefaultStack()),
                            x + random.nextDouble() - 0.5D,
                            y - 1.0D,
                            z + random.nextDouble() - 0.5D,
                            1,
                            (random.nextDouble() - 0.5D) * 0.5D,
                            0.2D + random.nextDouble() * 0.6D,
                            (random.nextDouble() - 0.5D) * 0.5D,
                            0 // matches "iconcrack_352"
                    );
                }
            }

            // --- Feather explosion ---
            int featherCount = 3 + random.nextInt(3); // 3–5

            for (int i = 0; i < featherCount; i++) {
                ItemStack stack = new ItemStack(Items.FEATHER);

                double xPos = x + (random.nextDouble() - 0.5D) * 2D;
                double yPos = y + 0.5D;
                double zPos = z + (random.nextDouble() - 0.5D) * 2D;

                ItemEntity item = new ItemEntity(world, xPos, yPos, zPos, stack);

                item.setVelocity(
                        (random.nextDouble() - 0.5D) * 0.5D,
                        0.2D + random.nextDouble() * 0.3D,
                        (random.nextDouble() - 0.5D) * 0.5D
                );

                item.setPickupDelay(10);
                world.spawnEntity(item);
            }

            // --- Spread possession ---
            //spreadPossession(chicken, 16.0, new PossessionSource.EntityDeath(chicken));

            // --- Kill chicken ---
            chicken.discard();
        }

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
