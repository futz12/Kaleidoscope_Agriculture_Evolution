package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer.rope;

import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.model.rope.DiamondRopeModel;
import com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.model.rope.RopeModelData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import static com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.Kaleidoscope_Agriculture_Evolution.MODID;

public class RopeRendererImpl extends RopeRenderer {

    private static final ResourceLocation DEFAULT_ROPE_TEXTURE =
            new ResourceLocation(MODID, "textures/entity/lead_rope.png");

    @Override
    protected RopeModelData buildModel(Vec3 start, Vec3 end, float width, float sagFactor, int segments) {
        return DiamondRopeModel.buildModel(start, end, width, sagFactor, segments);
    }

    @Override
    protected ResourceLocation getDefaultTexture() {
        return DEFAULT_ROPE_TEXTURE;
    }
}