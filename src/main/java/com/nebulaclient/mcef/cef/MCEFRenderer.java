package com.nebulaclient.mcef.cef;

import com.mojang.blaze3d.platform.GlStateManager;
import com.nebulaclient.mcef.MCEF;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class MCEFRenderer {
    private final boolean transparent;
    private int textureID = 0;
    private boolean unpainted = true;

    private int currentWidth = 1;
    private int currentHeight = 1;

    private static final int DEFAULT_WIDTH = 1;
    private static final int DEFAULT_HEIGHT = 1;

    private static final byte[] TRANSPARENT_PIXELS = new byte[] {0, 0, 0, 0};

    protected MCEFRenderer(boolean transparent) {
        this.transparent = transparent;
    }

    public void initialize() {
        if (textureID != 0) {
            glDeleteTextures(textureID);
        }

        textureID = glGenTextures();
        GlStateManager.bindTexture(textureID);

        // Set texture params
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        ByteBuffer initialData = ByteBuffer.allocateDirect(4);
        if (transparent) {
            initialData.put(TRANSPARENT_PIXELS);
        } else {
            initialData.put(new byte[] {0, 0, 0, (byte)255});
        }
        initialData.flip();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, DEFAULT_WIDTH, DEFAULT_HEIGHT, 0,
                GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, initialData);

        GlStateManager.bindTexture(0);
        currentWidth = DEFAULT_WIDTH;
        currentHeight = DEFAULT_HEIGHT;
        unpainted = true;
    }

    public int getTextureID() {
        return textureID;
    }

    public boolean isUnpainted() {
        return unpainted;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public int getCurrentWidth() {
        return currentWidth;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }

    protected void cleanup() {
        if (textureID != 0) {
            glDeleteTextures(textureID);
            textureID = 0;
        }
    }

    public void renderToTexture() {
        MCEF.INSTANCE.getApp().getHandle().N_DoMessageLoopWork();
    }

    protected void onPaint(ByteBuffer buffer, int width, int height) {
        if (textureID == 0) {
            initialize();
        }

        GlStateManager.pushMatrix();

        try {
            GlStateManager.bindTexture(textureID);

            boolean dimensionsChanged = width != currentWidth || height != currentHeight;

            if (dimensionsChanged) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                        GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
                currentWidth = width;
                currentHeight = height;
            } else {
                GL11.glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
                GL11.glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
                GL11.glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
                glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height,
                        GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
            }

            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            unpainted = false;

            GlStateManager.disableBlend();
        } catch (Exception e) {
            MCEF.INSTANCE.getLogger().error("Error updating browser texture", e);
        } finally {
            GlStateManager.bindTexture(0);
            GlStateManager.popMatrix();
        }
    }

    protected void onPaint(ByteBuffer buffer, int x, int y, int width, int height) {
        if (textureID == 0) {
            return;
        }

        //No painting outside, don't remember if this is the only reason it looked weird before
        if (x + width > currentWidth || y + height > currentHeight) {
            return;
        }

        try {
            GlStateManager.pushMatrix();
            GlStateManager.bindTexture(textureID);

            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

            glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
            unpainted = false;

            GlStateManager.disableBlend();
            GlStateManager.bindTexture(0);
            GlStateManager.popMatrix();
        } catch (Exception e) {
            MCEF.INSTANCE.getLogger().error("Error updating browser texture region", e);
        }
    }
}