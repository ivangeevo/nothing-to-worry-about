package org.btwr.ntwa.entity.possession;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.btwr.ntwa.data.PossessionData;

public class SquidPossessionBehavior {

    public static void tick(LivingEntity livingEntity, PossessionData data) {
        if (!(livingEntity instanceof SquidEntity squid)) return;
        if (!data.isFullyPossessed()) return;

        // countdown to next leap
        if (data.getSquidLeapCountdown() > 0) {
            data.setSquidLeapCountdown(data.getSquidLeapCountdown() - 1);
        }

        // propulsion phase (THIS is what you're missing)
        if (data.getSquidPropulsionTicks() > 0) {
            double speed = 0.6;

            squid.setVelocity(
                    data.getSquidLaunchX() * speed,
                    1.0,
                    data.getSquidLaunchZ() * speed
            );

            squid.velocityModified = true;
            Vec3d vel = squid.getVelocity();

            float yaw = (float)(Math.atan2(vel.z, vel.x) * (180 / Math.PI)) - 90f;
            float pitch = (float)(-(Math.atan2(vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z)) * (180 / Math.PI)));

            //squid.setYaw(yaw);
            //squid.setPitch(pitch);
            squid.setYaw(MathHelper.lerp(0.3f, squid.getYaw(), yaw));
            squid.setPitch(MathHelper.lerp(0.3f, squid.getPitch(), pitch));
            squid.bodyYaw = yaw;

            data.setSquidPropulsionTicks(data.getSquidPropulsionTicks() - 1);
        }

        // try to start a leap
        if (data.getSquidLeapCountdown() <= 0 && squid.isTouchingWater()) {
            startLeap(squid, data);
        }

        // ghast conversion
        tryGhastTransform(squid, data);
    }

    private static void startLeap(SquidEntity squid, PossessionData data) {
        data.setSquidLeapCountdown(200); // 10 seconds

        data.setSquidGhastRoll(squid.getRandom().nextFloat());

        if (squid.isTouchingWater()) {
            data.setSquidPropulsionTicks(10);

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

            data.setSquidLaunchX(dir.x);
            data.setSquidLaunchZ(dir.z);

        } else {
            data.setSquidPropulsionTicks(0);

            squid.playSound(
                    SoundEvents.ENTITY_SLIME_JUMP,
                    1.0F,
                    0.5F + squid.getRandom().nextFloat() * 0.1F
            );
        }
    }

    private static void tryGhastTransform(SquidEntity squid, PossessionData data) {
        if (squid.getWorld().isClient()) return;

        // must be falling after leap
        if (squid.isTouchingWater()) return;
        if (squid.getVelocity().y > 0) return;

        if (data.getSquidGhastRoll() > 0.25f) return;

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
