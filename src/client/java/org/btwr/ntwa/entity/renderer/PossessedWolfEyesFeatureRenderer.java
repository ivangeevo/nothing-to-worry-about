package org.btwr.ntwa.entity.renderer;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.WolfEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.Identifier;
import org.btwr.ntwa.NTWAMod;
import org.btwr.ntwa.data.ModDataAttachments;
import org.btwr.ntwa.data.PossessionData;

public class PossessedWolfEyesFeatureRenderer extends FeatureRenderer<WolfEntity, WolfEntityModel<WolfEntity>> {

    private static final Identifier TEXTURE =
            Identifier.of(NTWAMod.MOD_ID, "textures/entity/wolf/wolf_possessed_eyes.png");

    public PossessedWolfEyesFeatureRenderer(
            FeatureRendererContext<WolfEntity, WolfEntityModel<WolfEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       WolfEntity wolf,
                       float limbAngle,
                       float limbDistance,
                       float tickDelta,
                       float animationProgress,
                       float headYaw,
                       float headPitch
    ) {

        PossessionData data = wolf.getAttached(ModDataAttachments.POSSESSABLE);
        if (data == null || !data.isPossessed()) return;

        PossessionData.WolfData wolfData = data.getOrCreate(PossessionData.WolfData.class, PossessionData.WolfData::new);

        if (wolfData == null) return;

        if (!wolfData.didHeadSpin || wolfData.headRotation == 0) return;

        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEyes(TEXTURE));

        this.getContextModel().render(matrices, vc, 0xF000F0, OverlayTexture.DEFAULT_UV);
    }

}