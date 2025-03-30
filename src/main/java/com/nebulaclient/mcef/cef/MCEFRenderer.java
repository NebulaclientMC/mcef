/*
 * MCEF (Minecraft Chromium Embedded Framework)
 * Copyright (C) 2025 CCBlueX
 * Copyright (C) 2023 CinemaMod Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 * USA
 */
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

    protected MCEFRenderer(boolean transparent) {
        this.transparent = transparent;
    }

    public void initialize() {
        textureID = glGenTextures();
        GlStateManager.bindTexture(textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GlStateManager.bindTexture(0);
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
            return;
        }

        GlStateManager.pushMatrix();
        if (transparent) {
            GlStateManager.enableBlend();
        }

        GlStateManager.bindTexture(textureID);
        GL11.glPixelStorei(GL_UNPACK_ROW_LENGTH, width);
        GL11.glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        unpainted = false;

        GlStateManager.popMatrix();
    }

    protected void onPaint(ByteBuffer buffer, int x, int y, int width, int height) {
        GlStateManager.pushMatrix();
        GlStateManager.bindTexture(textureID);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, GL_BGRA,
                GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        unpainted = false;
        GlStateManager.popMatrix();
    }
}