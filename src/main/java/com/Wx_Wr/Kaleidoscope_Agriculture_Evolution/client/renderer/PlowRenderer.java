package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.model.PlowModel;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.entity.PlowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PlowRenderer extends GeoEntityRenderer<PlowEntity> {

    public PlowRenderer(EntityRendererProvider.Context context) {
        super(context, new PlowModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(PlowEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        super.render(entity, 0, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}