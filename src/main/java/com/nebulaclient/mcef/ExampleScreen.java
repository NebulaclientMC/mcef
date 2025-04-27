/***
 * Copyright (C) for NebulaClient
 */

package com.nebulaclient.mcef;

import com.mojang.blaze3d.platform.GlStateManager;
import com.nebulaclient.lwjgl3.implementation.glfw.GLFWKeyboardImplementation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

import com.nebulaclient.mcef.cef.MCEFBrowser;

public class ExampleScreen extends Screen {
    private static final int DRAW_OFFSET = 0;

    // Instance variables
    private final MinecraftClient client = MinecraftClient.getInstance();
    private MCEFBrowser browser;
    private Identifier texture;

    private int prevMouseX = 0;
    private int prevMouseY = 0;

    private boolean isPressLeft = false;
    private boolean isPressRight = false;
    private boolean isPressMiddle = false;

    // New smooth scrolling variables - built from scratch
    private double scrollAccumulator = 0.0;
    private double scrollVelocity = 0.0;
    private long lastScrollEventTime = 0;
    private long lastFrameTime = System.currentTimeMillis();
    private boolean isInScrollingAnimation = false;

    private static final double SCROLL_MULTIPLIER = 1;
    private static final double SCROLL_FRICTION = 0.98;
    private static final double SCROLL_MIN_VELOCITY = 0.1;
    private static final double MAX_VELOCITY =5.0;
    private static final long SCROLL_TIMEOUT = 100;

    private final boolean[] keyPressed = new boolean[Keyboard.KEYBOARD_SIZE];

    public ExampleScreen() {
        super();
    }

    @Override
    public void init() {
        super.init();
        if (browser == null) {
            String url = "http://localhost:3000/";
            boolean transparent = true;

            browser = MCEF.INSTANCE.createBrowser(url, transparent, 1000);
            texture = new Identifier("mcef", "browser/tab/" + browser.hashCode());

            client.getTextureManager().loadTexture(texture, new AbstractTexture() {
                @Override
                public void load(ResourceManager manager) throws IOException {

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
        return (int) ((x - DRAW_OFFSET) * getScaleFactor());
    }

    private int mouseY(int y) {
        return (int) ((y - DRAW_OFFSET) * getScaleFactor());
    }

    private int scaleX(int x) {
        return (int) ((x - DRAW_OFFSET * 2) * getScaleFactor());
    }

    private int scaleY(int y) {
        return (int) ((y - DRAW_OFFSET * 2) * getScaleFactor());
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

        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - lastFrameTime) / 1000.0;
        lastFrameTime = currentTime;

        // Cap deltaTime to prevent large jumps
        if (deltaTime > 0.1) {
            deltaTime = 0.1;
        }

        // Handle mouse movement
        if (prevMouseX != mouseX || prevMouseY != mouseY) {
            browser.sendMouseMove(mouseX(mouseX), mouseY(mouseY));
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }

        // Handle left mouse button
        boolean isLeftDown = Mouse.isButtonDown(0);
        if (isLeftDown && !isPressLeft) {
            browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), 0);
            browser.setFocus(true);
            isPressLeft = true;
        } else if (!isLeftDown && isPressLeft) {
            browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), 0);
            isPressLeft = false;
        }

        // Handle right mouse button
        boolean isRightDown = Mouse.isButtonDown(1);
        if (isRightDown && !isPressRight) {
            browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), 1);
            browser.setFocus(true);
            isPressRight = true;
        } else if (!isRightDown && isPressRight) {
            browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), 1);
            isPressRight = false;
        }

        // Handle middle mouse button
        boolean isMiddleDown = Mouse.isButtonDown(2);
        if (isMiddleDown && !isPressMiddle) {
            browser.sendMousePress(mouseX(mouseX), mouseY(mouseY), 2);
            browser.setFocus(true);
            isPressMiddle = true;
        } else if (!isMiddleDown && isPressMiddle) {
            browser.sendMouseRelease(mouseX(mouseX), mouseY(mouseY), 2);
            isPressMiddle = false;
        }

        //mouse wheel scrolling
        handleSmoothScrolling(mouseX, mouseY, deltaTime);

        // Render browser content to texture
        browser.getRenderer().renderToTexture();

        // Draw the texture
        GlStateManager.pushMatrix();
        GlStateManager.disableDepthTest();
        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();

        client.getTextureManager().bindTexture(texture);
        Screen.drawTexture(0, 0, 0, 0, width, height, width, height);

        GlStateManager.disableAlphaTest();
        GlStateManager.enableDepthTest();
        GlStateManager.popMatrix();
    }

    //This still needs work!
    private void handleSmoothScrolling(int mouseX, int mouseY, double deltaTime) {
        //This uses our custom lwjgl3 for 1.8.9
        double wheelDelta = Mouse.getOriginalMouseEventWheel();
        if (wheelDelta != 0.0) {
            lastScrollEventTime = System.currentTimeMillis();
            isInScrollingAnimation = true;

            double scrollInput = wheelDelta * SCROLL_MULTIPLIER;

            scrollVelocity += scrollInput;

            if (scrollVelocity > MAX_VELOCITY) {
                scrollVelocity = MAX_VELOCITY;
            } else if (scrollVelocity < -MAX_VELOCITY) {
                scrollVelocity = -MAX_VELOCITY;
            }
        }

        if (isInScrollingAnimation) {
            double frameScrollAmount = scrollVelocity * deltaTime * 60.0; //Todo: Use minecrafts fps i guess

            scrollAccumulator += frameScrollAmount;

            double scrollPixels = scrollAccumulator;
            if (scrollPixels != 0) {
                browser.sendMouseWheel(mouseX(mouseX), mouseY(mouseY),wheelDelta, 0);

                scrollAccumulator -= scrollPixels;
            }

            scrollVelocity *= Math.pow(SCROLL_FRICTION, deltaTime * 60.0);

            long timeSinceLastScroll = System.currentTimeMillis() - lastScrollEventTime;
            if (Math.abs(scrollVelocity) < SCROLL_MIN_VELOCITY || timeSinceLastScroll > SCROLL_TIMEOUT) {
                scrollVelocity = 0.0;
                scrollAccumulator = 0.0;
                isInScrollingAnimation = false;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        // Handle in render method, will stay there for now until i make new Callbacks from the GLFW functions
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        // Handle in render method, will stay there for now until i make new Callbacks from the GLFW functions
    }

    @Override
    public void handleMouse() {
        super.handleMouse();
    }

    @Override
    protected void keyPressed(char character, int keyCode) {
        browser.setFocus(true);

        if (keyCode == Keyboard.KEY_ESCAPE) {
            browser.close();
            super.keyPressed(character, keyCode);
            return;
        }

        if (character != 0) {
            browser.sendKeyTyped(character, getModifiers());
        }
    }

    @Override
    public void handleKeyboard() {
        browser.setFocus(true);

        int keyCode = Keyboard.getEventKey();
        char keyChar = Keyboard.getEventCharacter();
        boolean isKeyDown = Keyboard.getEventKeyState();
        int modifiers = getModifiers();

        if (keyCode != -1) {
            int glfwKey = GLFWKeyboardImplementation.translateKeyToGLFaW(keyCode);
            int scanCode = GLFW.glfwGetKeyScancode(glfwKey);

            if (isKeyDown) {
                browser.sendKeyPress(glfwKey, scanCode, modifiers);
            } else {
                browser.sendKeyRelease(glfwKey, scanCode, modifiers);
            }
        }
        super.handleKeyboard();
    }

    @Override
    public boolean shouldPauseGame() {
        return false;
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