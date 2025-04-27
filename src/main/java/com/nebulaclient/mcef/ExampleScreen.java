/*
 *     MCEF (Minecraft Chromium Embedded Framework)
 *     Copyright (C) 2023 CinemaMod Group
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */

package com.nebulaclient.mcef;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nebulaclient.mcef.cef.MCEFBrowser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.UUID;
import java.util.function.Function;

import static com.nebulaclient.mcef.utils.ExtendedRenderLayers.JCEF_BLEND;


public class ExampleScreen extends Screen {
    private static final int drawOffset = 0;

    MinecraftClient minecraft = MinecraftClient.getInstance();

    private MCEFBrowser browser;
    private Identifier texture;

    protected ExampleScreen() {
        super(Text.of("MCEF/"+UUID.randomUUID().toString().split("-")[0]));

    }
    private final Gson gson = new Gson();



    @Override
    protected void init() {
        super.init();
        if (browser == null) {
            String url = "http://127.0.0.1:3000/";
            boolean transparent = true;

            browser = MCEF.INSTANCE.createBrowser(url, transparent, 240);
            texture = Identifier.of("mcef" , "browser/tab/" + browser.hashCode());

            minecraft.getTextureManager().registerTexture(texture, new AbstractTexture() {
                @Override
                public int getGlId() {
                    return browser.getRenderer().getTextureID();
                }
            });

            resizeBrowser();

        }
    }

    private int mouseX(double x) {
        return (int) ((x - drawOffset) * minecraft.getWindow().getScaleFactor());
    }

    private int mouseY(double y) {
        return (int) ((y - drawOffset) * minecraft.getWindow().getScaleFactor());
    }

    private int scaleX(double x) {
        return (int) ((x - drawOffset * 2) * minecraft.getWindow().getScaleFactor());
    }

    private int scaleY(double y) {
        return (int) ((y - drawOffset * 2) * minecraft.getWindow().getScaleFactor());
    }

    private void resizeBrowser() {
        if (width > 100 && height > 100) {
            browser.resize(scaleX(width), scaleY(height));
        }
    }

    @Override
    public void resize(MinecraftClient minecraft, int i, int j) {
        super.resize(minecraft, i, j);
        resizeBrowser();
    }


    @Override
    public void close() {
        browser.close();
        super.close();
    }

    @Override
    public void render(DrawContext guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        browser.getRenderer().renderToTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        guiGraphics.drawTexture(BLURRED_TEXTURE_LAYER, texture, 0, 0, 0f, 0f, width - 0,
               height - 0, width - 0,height - 0);
        RenderSystem.enableDepthTest();
        System.err.println("H");
        Gson gson = new Gson();
    }

    private static final Function<Identifier, RenderLayer> BLURRED_TEXTURE_LAYER = Util.memoize(textureId -> RenderLayer.of("blurred_ui_layer", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 786432, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(textureId, TriState.FALSE, false)).transparency(JCEF_BLEND).program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM).depthTest(RenderPhase.LEQUAL_DEPTH_TEST).target(RenderPhase.MAIN_TARGET).build(false)));

    public static Function<Identifier, RenderLayer> getBlurredTextureLayer() {
        return BLURRED_TEXTURE_LAYER;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), button);
        browser.setFocus(true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), button);
        browser.setFocus(true);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        browser.sendMouseMove(mouseX(mouseX), mouseY(mouseY));
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        browser.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), scrollY, 0);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        browser.sendKeyPress(keyCode, scanCode, modifiers);
        browser.setFocus(true);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        browser.sendKeyRelease(keyCode, scanCode, modifiers);
        browser.setFocus(true);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint == (char) 0) return false;
        browser.sendKeyTyped(codePoint, modifiers);
        browser.setFocus(true);
        return super.charTyped(codePoint, modifiers);
    }
}
