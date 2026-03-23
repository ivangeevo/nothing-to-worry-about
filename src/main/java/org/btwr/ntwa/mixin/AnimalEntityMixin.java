package org.btwr.ntwa.mixin;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.btwr.ntwa.entity.possession.CowPossessionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin {

    @Inject(
            method = "breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"),
            cancellable = true
    )
    private void corruptCowBreeding(ServerWorld world, AnimalEntity other, CallbackInfo ci) {
        AnimalEntity self = (AnimalEntity)(Object)this;

        // only care about cows
        if (!(self instanceof CowEntity) && !(other instanceof CowEntity)) return;

        Vec3d pos = self.getPos();

        if (CowPossessionBehavior.tryCorruptCowBirth(self, other, world, pos)) {
            // replicate vanilla post-breed effects
            self.setBreedingAge(6000);
            other.setBreedingAge(6000);

            self.resetLoveTicks();
            other.resetLoveTicks();

            world.sendEntityStatus(self, EntityStatuses.ADD_BREEDING_PARTICLES);

            if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                world.spawnEntity(new ExperienceOrbEntity(
                        world,
                        self.getX(),
                        self.getY(),
                        self.getZ(),
                        self.getRandom().nextInt(7) + 1
                ));

                // kill the vanilla child spawn
                ci.cancel();
            }
        }
    }
}
