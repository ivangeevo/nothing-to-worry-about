package org.btwr.ntwa.mixin.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.entity.passive.WolfEntity;
import org.btwr.ntwa.entity.renderer.PossessedWolfEyesFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfEntityRenderer.class)
public abstract class WolfEntityRendererMixin
        extends MobEntityRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {

    public WolfEntityRendererMixin(
            EntityRendererFactory.Context ctx,
            WolfEntityModel<WolfEntity> model,
            float shadowRadius
    ) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ntwa$addFeature(EntityRendererFactory.Context context, CallbackInfo ci) {
        this.addFeature(new PossessedWolfEyesFeatureRenderer(this));
    }
}