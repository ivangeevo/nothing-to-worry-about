package org.btwr.ntwa.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.entity.passive.WolfEntity;
import org.btwr.ntwa.data.ModDataAttachments;
import org.btwr.ntwa.data.PossessionData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfEntityModel.class)
public abstract class WolfEntityModelMixin {

    @Shadow @Final private ModelPart realHead;

    @Inject(method = "animateModel(Lnet/minecraft/entity/passive/WolfEntity;FFF)V", at = @At("TAIL"))
    private void applyPossessionHeadSpin(WolfEntity wolfEntity, float f, float g, float h, CallbackInfo ci) {
        var data = wolfEntity.getAttached(ModDataAttachments.POSSESSABLE);
        if (data == null) return;

        var wolfData = data.getOrCreate(PossessionData.WolfData.class, PossessionData.WolfData::new);
        if (!wolfData.attempting) return;

        // Fully override roll
        this.realHead.roll = wolfData.headRotation % (float)(Math.PI * 2);
    }
}