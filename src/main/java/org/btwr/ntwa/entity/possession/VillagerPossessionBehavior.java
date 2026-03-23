package org.btwr.ntwa.entity.possession;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.VillagerEntity;

public class VillagerPossessionBehavior {

    public static void onFullPossession(VillagerEntity villager) {
        if (villager.getWorld().isClient) return; // Only run on the server

        // Play transformation particle/sound effect
        villager.getWorld().sendEntityStatus(villager, (byte) 60); // optional: custom byte for your effect

        // Remove the original villager
        villager.remove(Entity.RemovalReason.DISCARDED);

        // Create the Witch entity
        WitchEntity witch = EntityType.WITCH.create(villager.getWorld());
        if (witch == null) return;

        // Set Witch position and rotation to match Villager
        witch.refreshPositionAndAngles(
                villager.getX(), villager.getY(), villager.getZ(), villager.getYaw(), villager.getPitch()
        );
        witch.setYaw(villager.getYaw());
        witch.setPitch(villager.getPitch());

        // Make persistent so it doesn’t despawn
        witch.setPersistent();

        // Spawn the Witch in the world
        villager.getWorld().spawnEntity(witch);
    }

}