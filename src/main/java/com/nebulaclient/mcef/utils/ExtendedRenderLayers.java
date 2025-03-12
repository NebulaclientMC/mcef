package com.nebulaclient.mcef.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderPhase;

public class ExtendedRenderLayers {

    public static final RenderPhase.Transparency JCEF_BLEND = new RenderPhase.Transparency("jcef_compatible_blend", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    });


}
