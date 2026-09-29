package org.btwr.ntwa.entity.possession.behavior;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.world.World;

public class VillagerPossessionBehavior {

    public static void onFullPossession(VillagerEntity villager) {
        World world = villager.getWorld();
        
        if (world.isClient) return;

        // Play transformation particle/sound effect
        world.sendEntityStatus(villager, (byte) 60);

        // Remove the original villager
        villager.remove(Entity.RemovalReason.DISCARDED);

        WitchEntity witch = EntityType.WITCH.create(world);
        if (witch == null) return;

        // Set Witch position and rotation to match Villager
        witch.refreshPositionAndAngles(
                villager.getX(), villager.getY(), villager.getZ(), villager.getYaw(), villager.getPitch()
        );
        witch.setYaw(villager.getYaw());
        witch.setPitch(villager.getPitch());

        // Make persistent so it doesn’t despawn
        witch.setPersistent();
        world.spawnEntity(witch);
    }

}