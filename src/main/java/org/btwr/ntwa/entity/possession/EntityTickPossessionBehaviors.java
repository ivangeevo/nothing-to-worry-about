package org.btwr.ntwa.entity.possession;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionTypes;
import org.btwr.ntwa.data.PossessionData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class EntityTickPossessionBehaviors {

    public static final Map<EntityType<?>, BiConsumer<LivingEntity, PossessionData>> TICK_BEHAVIORS = new HashMap<>();

    public static void register() {
        TICK_BEHAVIORS.put(EntityType.SHEEP, EntityTickPossessionBehaviors::tickSheep);
    }

    private static void tickSheep(LivingEntity entity, PossessionData data) {
        if (!(entity instanceof SheepEntity sheep)) return;
        if (!data.isFullyPossessed() || sheep.isSheared() || sheep.isDead()) return;

        // Skip if in water/lava
        if (sheep.isTouchingWater() || sheep.isInLava()) return;

        var vel = sheep.getVelocity();

        // vertical lift based on Y position
        double lift = (sheep.getY() < 125.0 ? 0.08341 : 0.0725);
        vel = vel.add(0, lift, 0); // cumulative

        // Horizontal drift in Overworld above 100
        if (!sheep.isOnGround() && !sheep.horizontalCollision
                && sheep.getWorld().getDimensionEntry().matchesId(DimensionTypes.OVERWORLD_ID)) {
            if (sheep.getY() > 100.0) {
                double driftX = vel.x > -0.012 ? vel.x - 0.005 : vel.x;
                vel = new Vec3d(driftX, vel.y, vel.z);
            }
        }

        // Apply back to entity
        sheep.setVelocity(vel);
        sheep.velocityModified = true;
        sheep.fallDistance = 0;

        // Let vanilla gravity partially apply
        sheep.setNoGravity(false);
        sheep.setOnGround(false); // mimic BTW’s airborne state
    }

}
