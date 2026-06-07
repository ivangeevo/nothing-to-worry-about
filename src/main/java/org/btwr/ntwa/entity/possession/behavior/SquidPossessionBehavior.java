package org.btwr.ntwa.entity.possession.behavior;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.btwr.ntwa.data.PossessionData;

public class SquidPossessionBehavior {

    public static void tick(LivingEntity livingEntity, PossessionData data) {
        if (!(livingEntity instanceof SquidEntity squid)) return;
        if (!data.isFullyPossessed()) return;

        var squidData = data.getOrCreate(PossessionData.SquidData.class, PossessionData.SquidData::new);

        // countdown to next leap
        if (squidData.leapCountdown > 0) {
            squidData.leapCountdown--;
        }

        // propulsion phase
        if (squidData.propulsionTicks > 0) {
            double speed = 0.6;

            squid.setVelocity(squidData.launchX * speed, 1.0, squidData.launchZ * speed);

            squid.velocityModified = true;
            Vec3d vel = squid.getVelocity();

            float yaw = (float)(Math.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90f;
            float pitch = (float)(-(Math.atan2(vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z)) * (180 / Math.PI)));

            squid.setYaw(MathHelper.lerp(0.3f, squid.getYaw(), yaw));
            squid.setPitch(MathHelper.lerp(0.3f, squid.getPitch(), pitch));
            squid.bodyYaw = yaw;

            squidData.propulsionTicks--;
        }

        // try to start a leap
        if (squidData.leapCountdown <= 0 && squid.isTouchingWater()) {
            startLeap(squid, squidData);
        }

        // ghast conversion
        tryGhastTransform(squid, squidData);
    }

    private static void startLeap(SquidEntity squid, PossessionData.SquidData squidData) {
        squidData.leapCountdown = 200; // 10 seconds

        squidData.ghastRoll = squid.getRandom().nextFloat();

        if (squid.isTouchingWater()) {
            squidData.propulsionTicks = 10;

            squid.playSound(
                    SoundEvents.ENTITY_GENERIC_SPLASH,
                    1.0F,
                    0.5F + squid.getRandom().nextFloat() * 0.1F
            );

            Vec3d dir = Vec3d.fromPolar(0, squid.getYaw()).normalize();

            // small randomness so they don’t all go straight
            dir = dir.add(
                    (squid.getRandom().nextDouble() - 0.5) * 0.3,
                    0,
                    (squid.getRandom().nextDouble() - 0.5) * 0.3
            ).normalize();

            squidData.launchX = dir.x;
            squidData.launchZ = dir.z;

        } else {
            squidData.propulsionTicks = 0;

            squid.playSound(
                    SoundEvents.ENTITY_SLIME_JUMP,
                    1.0F,
                    0.5F + squid.getRandom().nextFloat() * 0.1F
            );
        }
    }

    private static void tryGhastTransform(SquidEntity squid, PossessionData.SquidData squidData) {
        if (squid.getWorld().isClient()) return;

        // must be falling after leap
        if (squid.isTouchingWater()) return;
        if (squid.getVelocity().y > 0) return;

        if (squidData.ghastRoll > 0.25f) return;

        ServerWorld world = (ServerWorld) squid.getWorld();

        GhastEntity ghast = EntityType.GHAST.create(world);
        if (ghast == null) return;

        ghast.refreshPositionAndAngles(
                squid.getX(),
                squid.getY(),
                squid.getZ(),
                squid.getYaw(),
                0f
        );

        ghast.setPersistent();

        world.spawnEntity(ghast);
        squid.remove(Entity.RemovalReason.DISCARDED);
    }

}
