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

import com.mojang.blaze3d.platform.GlStateManager;
import com.nebulaclient.mcef.cef.MCEFBrowser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;



public class ExampleScreen extends Screen {
    private static final int BROWSER_DRAW_OFFSET = 20;

    MinecraftClient minecraft = MinecraftClient.getInstance();

    private MCEFBrowser browser;
    private Identifier texture;

    public ExampleScreen() {
    }

    @Override
    public void init() {
        super.init();
        if (browser == null) {
            String url = "http://127.0.0.1:3000/ui/performanceTest.html";
            boolean transparent = false;

            browser = MCEF.INSTANCE.createBrowser(url, false, 1000);
            browser.getRenderer().initialize();

            texture = new Identifier("mcef", "browser/tab/" + browser.hashCode());

            MinecraftClient.getInstance().getTextureManager().loadTexture(texture, new AbstractTexture() {
                @Override
                public void load(ResourceManager manager) throws IOException {
                    return;
                }

                @Override
                public int getGlId() {
                    return browser.getRenderer().getTextureID();
                }
            });


            resizeBrowser();
        }
    }

    private void saveTextureToFile(int textureId, String filePath) {
        int width = 1024; // Adjust based on your texture size
        int height = 512;

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (x + (width * (height - y - 1))) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;
                int pixel = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, pixel);
            }
        }

        try {
            ImageIO.write(image, "PNG", new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int mouseX(double x) {
        return (int) ((x - BROWSER_DRAW_OFFSET) * new Window(MinecraftClient.getInstance()).getScaleFactor());
    }

    private int mouseY(double y) {
        return (int) ((y - BROWSER_DRAW_OFFSET) * new Window(MinecraftClient.getInstance()).getScaleFactor());
    }

    private int scaleX(double x) {
        return (int) ((x - BROWSER_DRAW_OFFSET * 2) * new Window(MinecraftClient.getInstance()).getScaleFactor());
    }

    private int scaleY(double y) {
        return (int) ((y - BROWSER_DRAW_OFFSET * 2) * new Window(MinecraftClient.getInstance()).getScaleFactor());
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


  /**
    public void close() {
        browser.close();
        super.close();
    }**/

    @Override
    public void render(int mouseX, int mouseY, float tickDelta) {
        super.render(mouseX, mouseY, tickDelta);
        browser.sendMouseMove(mouseX(mouseX), mouseY(mouseY));

        saveTextureToFile(browser.getRenderer().getTextureID(), "browser_texture.png");
        browser.getRenderer().renderToTexture();
        GlStateManager.disableDepthTest();
        GlStateManager.enableBlend();

        MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
        Screen.drawTexture(20, 20, 0f, 0f, width - 40,
                height - 40, width - 40,height - 40);
        MinecraftClient.getInstance().getTextureManager().bindTexture(texture);

        GlStateManager.bindTexture(0);
        GlStateManager.enableDepthTest();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), button);
        browser.setFocus(true);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), button);
        browser.setFocus(true);

        super.mouseReleased(mouseX, mouseY, button);
    }
    // private static final Function<Identifier, RenderLayer> BLURRED_TEXTURE_LAYER = Util.memoize(textureId -> RenderLayer.of("blurred_ui_layer", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS, 786432, RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(textureId, TriState.FALSE, false)).transparency(JCEF_BLEND).program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM).depthTest(RenderPhase.LEQUAL_DEPTH_TEST).target(RenderPhase.MAIN_TARGET).build(false)));

  /**  public static Function<Identifier, RenderLayer> getBlurredTextureLayer() {
        return BLURRED_TEXTURE_LAYER;
    }**/

  /***
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

    **/
}
