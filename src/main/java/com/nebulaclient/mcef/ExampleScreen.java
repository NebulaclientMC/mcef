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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import com.nebulaclient.mcef.cef.MCEFBrowser;

public class ExampleScreen extends Screen {
    private static final int drawOffset = 0;

    MinecraftClient client = MinecraftClient.getInstance();

    private MCEFBrowser browser;
    private Identifier texture;

    private int prevMouseX = 0;
    private int prevMouseY = 0;

    private boolean isPressLeft = false;
    private boolean isPressRight = false;
    private boolean isPressMiddle = false;

    public ExampleScreen() {
        super();
    }

    @Override
    public void init() {
        super.init();
        if (browser == null) {
            String url = "http://127.0.0.1:3000/ui/performanceTest.html";
            boolean transparent = true;

            browser = MCEF.INSTANCE.createBrowser(url, transparent, 1000);
            texture = new Identifier("mcef", "browser/tab/" + browser.hashCode());

            client.getTextureManager().loadTexture(texture, new AbstractTexture() {
                @Override
                public void load(ResourceManager manager) throws IOException {
                    //Why does it need to be there lmao
                }

                @Override
                public int getGlId() {
                    return browser.getRenderer().getTextureID();
                }
            });

            resizeBrowser();
        }
    }

    private int mouseX(int x) {
        return (int) ((x - drawOffset) * getScaleFactor());
    }

    private int mouseY(int y) {
        return (int) ((y - drawOffset) * getScaleFactor());
    }

    private int scaleX(int x) {
        return (int) ((x - drawOffset * 2) * getScaleFactor());
    }

    private int scaleY(int y) {
        return (int) ((y - drawOffset * 2) * getScaleFactor());
    }

    private float getScaleFactor() {
        return new Window(MinecraftClient.getInstance()).getScaleFactor();
    }

    private void resizeBrowser() {
        if (width > 100 && height > 100) {
            browser.resize(scaleX(width), scaleY(height));
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        resizeBrowser();
    }

    @Override
    public void removed() {
        browser.close();
        super.removed();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        super.render(mouseX, mouseY, delta);

        if (prevMouseX != mouseX || prevMouseY != mouseY) {
            browser.sendMouseMove(mouseX(mouseX), mouseY(mouseY));
        }
        prevMouseX = mouseX;
        prevMouseY = mouseY;

        boolean isLeftDown = Mouse.isButtonDown(0);
        if (isLeftDown && !isPressLeft) {
            browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), 0);
            browser.setFocus(true);
            isPressLeft = true;
        } else if (!isLeftDown && isPressLeft) {
            browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), 0);
            isPressLeft = false;
        }

        boolean isRightDown = Mouse.isButtonDown(1);
        if (isRightDown && !isPressRight) {
            browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), 1);
            browser.setFocus(true);
            isPressRight = true;
        } else if (!isRightDown && isPressRight) {
            browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), 1);
            isPressRight = false;
        }

        boolean isMiddleDown = Mouse.isButtonDown(2);
        if (isMiddleDown && !isPressMiddle) {
            browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), 2);
            browser.setFocus(true);
            isPressMiddle = true;
        } else if (!isMiddleDown && isPressMiddle) {
            browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), 2);
            isPressMiddle = false;
        }

        browser.getRenderer().renderToTexture();

        GlStateManager.disableDepthTest();
        GlStateManager.enableBlend();

        client.getTextureManager().bindTexture(texture);
        Screen.drawTexture(0, 0, 0, 0, width, height, width, height);

        GlStateManager.enableDepthTest();
    }


    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    //Need to fix that crap, scrolling sucks rn
    @Override
    public void handleMouse() {
        super.handleMouse();

        int mouseX = Mouse.getEventX() * this.width / this.client.width;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.client.height - 1;

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            browser.sendMouseWheel(mouseX(mouseX), mouseY(mouseY), scroll > 0 ? 1 : -1, 0);
        }
    }

    @Override
    protected void keyPressed(char id, int code) {
        browser.sendKeyPress(id, id, getModifiers());
        browser.setFocus(true);
        super.keyPressed(id, code);
    }

    private int getModifiers() {
        int modifiers = 0;

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            modifiers |= 1; // SHIFT
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            modifiers |= 2; // CTRL
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
            modifiers |= 4; // ALT
        }

        return modifiers;
    }
}