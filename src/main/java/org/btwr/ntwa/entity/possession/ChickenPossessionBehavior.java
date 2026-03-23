package org.btwr.ntwa.entity.possession;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ChickenPossessionBehavior {

    public static void onFullPossession(ChickenEntity chicken) {
        var world = chicken.getWorld();
        var random = chicken.getRandom();

        double x = chicken.getX();
        double y = chicken.getY();
        double z = chicken.getZ();

        // sounds
        if (!world.isClient()) {
            playExplodeSounds(chicken, random);
        }

        // particles
        if (world instanceof ServerWorld serverWorld) {
            spawnExplodeParticles(serverWorld, x, y, z, random);
        }

        // feather explosion
        dropFeathersOnExplode(world, x, y, z, random);

        // kill the chicken
        chicken.discard();
    }

    private static void playExplodeSounds(ChickenEntity chicken, Random random) {
        chicken.playSound(
                SoundEvents.ENTITY_GENERIC_EXPLODE.value(),
                1.0F,
                (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F
        );

        chicken.playSound(SoundEvents.ENTITY_CHICKEN_HURT, 2.0F, random.nextFloat() * 0.4F + 1.2F);
    }

    private static void spawnExplodeParticles(ServerWorld serverWorld, double x, double y, double z, Random random) {
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
                    0
            );
        }
    }

    private static void dropFeathersOnExplode(World world, double x, double y, double z, Random random) {
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
    }

}