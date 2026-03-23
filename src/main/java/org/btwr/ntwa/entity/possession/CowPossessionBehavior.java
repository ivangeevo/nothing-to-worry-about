package org.btwr.ntwa.entity.possession;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.btwr.ntwa.data.ModDataAttachments;

public class CowPossessionBehavior {

    public static boolean tryCorruptCowBirth(AnimalEntity parent, AnimalEntity mate, ServerWorld world, Vec3d pos) {
        var dataA = parent.getAttached(ModDataAttachments.POSSESSABLE);
        var dataB = mate.getAttached(ModDataAttachments.POSSESSABLE);

        boolean possessed =
                (dataA != null && dataA.isFullyPossessed()) ||
                        (dataB != null && dataB.isFullyPossessed());

        if (!possessed) return false;

        // 1/8 chance → normal baby (no corruption)
        if (world.random.nextInt(8) == 0) return false;

        // --- corruption happens ---
        boolean doMutant =
                world.getRegistryKey() != World.END &&
                        world.random.nextInt(2) == 0;

        if (doMutant) {
            spawnCorruptedBirth(world, pos);
        } else {
            spawnStillbornCow(world, pos);
        }

        return true; // cancel vanilla baby
    }

    private static void spawnCorruptedBirth(ServerWorld world, Vec3d pos) {
        var random = world.random;

        int roll = random.nextInt(20);

        if (roll == 0) {
            spawn(world, EntityType.CAVE_SPIDER, pos);
        } else if (roll < 4) {
            for (int i = 0; i < 10; i++) {
                spawn(world, EntityType.BAT, pos);
            }
        } else if (roll < 7) {
            for (int i = 0; i < 5; i++) {
                spawn(world, EntityType.SILVERFISH, pos);
            }
        } else {
            spawn(world, EntityType.SQUID, pos);
        }
    }

    private static void spawn(ServerWorld world, EntityType<?> type, Vec3d pos) {
        var entity = type.create(world);
        if (entity == null) return;

        entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        world.spawnEntity(entity);
    }

    private static void spawnStillbornCow(ServerWorld world, Vec3d pos) {
        var baby = EntityType.COW.create(world);
        if (baby == null) return;

        baby.setBaby(true);
        baby.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);

        world.spawnEntity(baby);

        baby.damage(world.getDamageSources().generic(), 20.0F);
    }

}