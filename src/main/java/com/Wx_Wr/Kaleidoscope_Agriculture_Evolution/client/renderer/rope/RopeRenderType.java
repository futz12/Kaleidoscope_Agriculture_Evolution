package com.Wx_Wr.Kaleidoscope_Agriculture_Evolution.client.renderer.rope;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class RopeRenderType {

    private RopeRenderType() {}

    public static RenderType rope(ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}