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
import com.nebulaclient.mcef.MCEFPlatform;
import com.nebulaclient.mcef.MCEF;
import com.nebulaclient.mcef.glfw.MCEFGlfwCursorHelper;
import com.nebulaclient.mcef.listeners.MCEFCursorChangeListener;


import org.cef.browser.CefBrowser;
import org.cef.browser.CefBrowserOsr;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefDragData;
import org.cef.event.CefKeyEvent;
import org.cef.event.CefMouseEvent;
import org.cef.event.CefMouseWheelEvent;
import org.cef.misc.CefCursorType;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * An instance of an "Off-screen rendered" Chromium web browser.
 * Complete with a renderer, keyboard and mouse inputs, optional
 * browser control shortcuts, cursor handling, drag & drop support.
 *
 * Rewritten @NebulaclientMC aka. Nebula Client
 */
public class MCEFBrowser extends CefBrowserOsr {
    /**
     * The renderer for the browser.
     */
    private final MCEFRenderer renderer;
    /**
     * Stores information about drag & drop.
     */
    private final MCEFDragContext dragContext = new MCEFDragContext();
    /**
     * A listener that defines that happens when a cursor changes in the browser.
     * E.g. when you've hovered over a button, an input box, are selecting text, etc...
     * A default listener is created in the constructor that sets the cursor type to
     * the appropriate cursor based on the event.
     */
    private MCEFCursorChangeListener cursorChangeListener;
    /**
     * Used to track when a full repaint should occur.
     */
    private int lastWidth = 0, lastHeight = 0;
    /**
     * A bitset representing what mouse buttons are currently pressed.
     * CEF is a bit odd and implements mouse buttons as a part of modifier flags.
     */
    private int btnMask = 0;

    // Data relating to popups and graphics
    // Marked as protected in-case a mod wants to extend MCEFBrowser and override the repaint logic
    protected ByteBuffer popupGraphics;
    protected Rectangle popupSize;
    protected boolean showPopup = false;
    protected boolean popupDrawn = false;
    private long lastClickTime = 0;
    private int clicks;
    private int mouseButton;

    private final boolean isMacOs = MCEFPlatform.getPlatform().isMacOS();

    public MCEFBrowser(MCEFClient client, String url, boolean transparent, int frameRate) {
        super(client.getHandle(), url, transparent, null, new MCEFBrowserSettings(frameRate));
        renderer = new MCEFRenderer(transparent);
        cursorChangeListener = (cefCursorID) -> setCursor(CefCursorType.fromId(cefCursorID));
        renderer.initialize();
    }

    public MCEFRenderer getRenderer() {
        return renderer;
    }

    public MCEFCursorChangeListener getCursorChangeListener() {
        return cursorChangeListener;
    }

    public void setCursorChangeListener(MCEFCursorChangeListener cursorChangeListener) {
        this.cursorChangeListener = cursorChangeListener;
    }

    public MCEFDragContext getDragContext() {
        return dragContext;
    }

    // Popups
    @Override
    public void onPopupShow(CefBrowser browser, boolean show) {
        super.onPopupShow(browser, show);
        showPopup = show;
        if (!show) popupDrawn = false;
    }

    @Override
    public void onPopupSize(CefBrowser browser, Rectangle size) {
        super.onPopupSize(browser, size);
        popupSize = size;
        this.popupGraphics = BufferUtils.createByteBuffer(
                size.width * size.height * 4
        );
    }

    @Override
    public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {
        if (dirtyRects.length == 0 || width <= 0 || height <= 0 || buffer == null) {
            return;
        }

        // Save state
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_ENABLE_BIT | GL11.GL_TEXTURE_BIT);
        GL11.glPushMatrix();

        // Save pixel storage parameters
        int[] origPackAlignment = new int[1];
        int[] origUnpackRowLength = new int[1];
        int[] origUnpackSkipPixels = new int[1];
        int[] origUnpackSkipRows = new int[1];

        GL11.glGetIntegerv(GL11.GL_PACK_ALIGNMENT, origPackAlignment);
        GL11.glGetIntegerv(GL11.GL_UNPACK_ROW_LENGTH, origUnpackRowLength);
        GL11.glGetIntegerv(GL11.GL_UNPACK_SKIP_PIXELS, origUnpackSkipPixels);
        GL11.glGetIntegerv(GL11.GL_UNPACK_SKIP_ROWS, origUnpackSkipRows);

        try {
            if (!popup) {
                if (lastWidth != width || lastHeight != height) {
                    lastWidth = width;
                    lastHeight = height;
                    renderer.onPaint(buffer, width, height);
                } else {
                    if (renderer.getTextureID() == 0) {
                        renderer.onPaint(buffer, width, height);
                        return;
                    }

                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderer.getTextureID());
                    GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, width);

                    for (Rectangle dirtyRect : dirtyRects) {
                        if (dirtyRect.width <= 0 || dirtyRect.height <= 0) continue;

                        if (dirtyRect.x >= width || dirtyRect.y >= height) continue;

                        int rectWidth = Math.min(dirtyRect.width, width - dirtyRect.x);
                        int rectHeight = Math.min(dirtyRect.height, height - dirtyRect.y);

                        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, dirtyRect.x);
                        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, dirtyRect.y);

                        renderer.onPaint(buffer, dirtyRect.x, dirtyRect.y, rectWidth, rectHeight);
                    }

                    // Handle popup
                    processPopup(buffer, width);
                }
            } else if (popupSize != null) {
                // Handle popup painting (direct to popup buffer)
                processPopupPaint(buffer, dirtyRects);
            }
        } catch (Exception e) {
            MCEF.INSTANCE.getLogger().error("Error during browser painting", e);
        } finally {
            // Restore pixel storage parameters
            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, origUnpackRowLength[0]);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, origUnpackSkipPixels[0]);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, origUnpackSkipRows[0]);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, origPackAlignment[0]);


            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            // Restore
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void processPopup(ByteBuffer buffer, int width) {
        if ((popupDrawn || showPopup) && popupSize != null) {
            if (!showPopup) {
                // Clear popup area when hiding
                if (popupSize.x < width && popupSize.y < renderer.getCurrentHeight()) {
                    GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, popupSize.x);
                    GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, popupSize.y);

                    int clearWidth = Math.min(popupSize.width, width - popupSize.x);
                    int clearHeight = Math.min(popupSize.height, renderer.getCurrentHeight() - popupSize.y);

                    if (clearWidth > 0 && clearHeight > 0) {
                        renderer.onPaint(buffer, popupSize.x, popupSize.y, clearWidth, clearHeight);
                    }
                }
                popupGraphics = null;
                popupSize = null;
                popupDrawn = false;
            } else if (popupDrawn && popupGraphics != null) {
                // Draw active popup
                GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, popupSize.width);
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);

                if (popupSize.x >= 0 && popupSize.y >= 0 &&
                        popupSize.x < renderer.getCurrentWidth() &&
                        popupSize.y < renderer.getCurrentHeight()) {

                    int renderWidth = Math.min(popupSize.width, renderer.getCurrentWidth() - popupSize.x);
                    int renderHeight = Math.min(popupSize.height, renderer.getCurrentHeight() - popupSize.y);

                    if (renderWidth > 0 && renderHeight > 0) {
                        renderer.onPaint(popupGraphics, popupSize.x, popupSize.y, renderWidth, renderHeight);
                    }
                }
            }
        }
    }

    private void processPopupPaint(ByteBuffer buffer, Rectangle[] dirtyRects) {
        if (renderer.getTextureID() == 0 || popupSize == null) return;

        if (popupGraphics == null || popupGraphics.capacity() < (popupSize.width * popupSize.height * 4)) {
            popupGraphics = BufferUtils.createByteBuffer(popupSize.width * popupSize.height * 4);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderer.getTextureID());

        for (Rectangle dirtyRect : dirtyRects) {
            if (dirtyRect.width <= 0 || dirtyRect.height <= 0) continue;

            // Calc source & destination positions in the buffers...
            int startX = Math.max(0, dirtyRect.x);
            int startY = Math.max(0, dirtyRect.y);
            int endX = Math.min(popupSize.width, dirtyRect.x + dirtyRect.width);
            int endY = Math.min(popupSize.height, dirtyRect.y + dirtyRect.height);

            if (startX >= endX || startY >= endY) continue;

            // popup must be within the texture bounds
            if (popupSize.x + startX >= renderer.getCurrentWidth() ||
                    popupSize.y + startY >= renderer.getCurrentHeight()) {
                continue;
            }

            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    int bufferPos = (y * popupSize.width + x) * 4;
                    int popupPos = (y * popupSize.width + x) * 4;

                    if (bufferPos >= 0 && bufferPos + 3 < buffer.capacity() &&
                            popupPos >= 0 && popupPos + 3 < popupGraphics.capacity()) {
                        popupGraphics.put(popupPos, buffer.get(bufferPos));     // B
                        popupGraphics.put(popupPos + 1, buffer.get(bufferPos + 1)); // G
                        popupGraphics.put(popupPos + 2, buffer.get(bufferPos + 2)); // R
                        popupGraphics.put(popupPos + 3, buffer.get(bufferPos + 3)); // A
                    }
                }
            }

            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, popupSize.width);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, startX);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, startY);

            int renderWidth = endX - startX;
            int renderHeight = endY - startY;

            if (popupSize.x + startX + renderWidth > renderer.getCurrentWidth()) {
                renderWidth = renderer.getCurrentWidth() - popupSize.x - startX;
            }

            if (popupSize.y + startY + renderHeight > renderer.getCurrentHeight()) {
                renderHeight = renderer.getCurrentHeight() - popupSize.y - startY;
            }

            if (renderWidth > 0 && renderHeight > 0) {
                renderer.onPaint(popupGraphics,
                        popupSize.x + startX,
                        popupSize.y + startY,
                        renderWidth,
                        renderHeight);
            }
        }

        popupDrawn = true;
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (browser_rect_.width != width || browser_rect_.height != height) {
            browser_rect_.setBounds(0, 0, width, height);

            lastWidth = 0;
            lastHeight = 0;

            wasResized(width, height);
        }
    }


    // Inputs
    public void sendKeyPress(int keyCode, long scanCode, int modifiers) {
        if (modifiers == GLFW_MOD_CONTROL && keyCode == GLFW_KEY_R) {
            reload();
            return;
        }

        CefKeyEvent e = new CefKeyEvent(CefKeyEvent.KEY_PRESS, keyCode, (char) keyCode, 0);
        e.scancode = scanCode;
        sendKeyEvent(e);
    }

    public void sendKeyRelease(int keyCode, long scanCode, int modifiers) {
        if (modifiers == GLFW_MOD_CONTROL && keyCode == GLFW_KEY_R) {
            return;
        }

        CefKeyEvent e = new CefKeyEvent(CefKeyEvent.KEY_RELEASE, keyCode, (char) keyCode, modifiers);
        e.scancode = scanCode;
        sendKeyEvent(e);
    }

    public void sendKeyTyped(char c, int modifiers) {
        if (modifiers == GLFW_MOD_CONTROL && (int) c == GLFW_KEY_R) {
            return;
        }

        CefKeyEvent e = new CefKeyEvent(CefKeyEvent.KEY_TYPE, c, c, modifiers);
        sendKeyEvent(e);
    }

    public void sendMouseMove(int mouseX, int mouseY) {
        sendMouseEvent(new CefMouseEvent(CefMouseEvent.MOUSE_MOVED, mouseX, mouseY, clicks, mouseButton,
                dragContext.getVirtualModifiers(btnMask)));

        if (dragContext.isDragging()) {
            this.dragTargetDragOver(new Point(mouseX, mouseY), 0, dragContext.getMask());
        }
    }

    public void sendMousePress(int mouseX, int mouseY, int button) {
        button = swapButton(button);

        if (button == 0) {
            btnMask |= CefMouseEvent.BUTTON1_MASK;
        } else if (button == 1) {
            btnMask |= CefMouseEvent.BUTTON2_MASK;
        } else if (button == 2) {
            btnMask |= CefMouseEvent.BUTTON3_MASK;
        }

        // Double click handling
        var time = System.currentTimeMillis();
        clicks = time - lastClickTime < 500 ? 2 : 1; //TODO: Uses system double click time setting

        sendMouseEvent(new CefMouseEvent(GLFW_PRESS, mouseX, mouseY, clicks, button, btnMask));

        this.lastClickTime = time;
        this.mouseButton = button;
    }

    // TODO:  Check why F Key goes fullscreen without FN modifier
    public void sendMouseRelease(int mouseX, int mouseY, int button) {
        button = swapButton(button);

        if (button == 0 && (btnMask & CefMouseEvent.BUTTON1_MASK) != 0) {
            btnMask ^= CefMouseEvent.BUTTON1_MASK;
        } else if (button == 1 && (btnMask & CefMouseEvent.BUTTON2_MASK) != 0) {
            btnMask ^= CefMouseEvent.BUTTON2_MASK;
        } else if (button == 2 && (btnMask & CefMouseEvent.BUTTON3_MASK) != 0) {
            btnMask ^= CefMouseEvent.BUTTON3_MASK;
        }

        // drag & drop
        if (dragContext.isDragging()) {
            if (button == 0) {
                finishDragging(mouseX, mouseY);
            }
        }

        sendMouseEvent(new CefMouseEvent(GLFW_RELEASE, mouseX, mouseY, clicks, button, btnMask));
        this.mouseButton = 0;
    }


    public void sendMouseWheel(int mouseX, int mouseY, double amount, int i) {
        // macOS generally has a slow scroll speed that feels more natural with their magic mice / trackpads
        if (!isMacOs) {
            // This removes the feeling of "smooth scroll"
            if (amount < 0) {
                amount = Math.floor(amount);
            } else {
                amount = Math.ceil(amount);
            }

            // This feels about equivalent to chromium with smooth scrolling disabled -ds58
            amount = amount * 3;
        }

        var event = new CefMouseWheelEvent(CefMouseWheelEvent.WHEEL_UNIT_SCROLL, mouseX, mouseY, amount, 0);
        sendMouseWheelEvent(event);
    }


    // Drag & drop
    @Override
    public boolean startDragging(CefBrowser browser, CefDragData dragData, int mask, int x, int y) {
        dragContext.startDragging(dragData, mask);
        this.dragTargetDragEnter(dragContext.getDragData(), new Point(x, y), btnMask, dragContext.getMask());
        // Indicates to CEF to not handle the drag event natively
        // reason: native drag handling doesn't work with off-screen rendering
        return false;
    }

    @Override
    public void updateDragCursor(CefBrowser browser, int operation) {
        if (dragContext.updateCursor(operation)) {
            // If the cursor to display for the drag event changes, then update the cursor
            this.onCursorChange(this, dragContext.getVirtualCursor(dragContext.getActualCursor()));
        }

        super.updateDragCursor(browser, operation);
    }

    public void startDragging(CefDragData dragData, int mask, int x, int y) {
        // Overload since the JCEF method requires a browser, which then goes unused
        startDragging(dragData, mask, x, y);
    }

    public void finishDragging(int x, int y) {
        dragTargetDrop(new Point(x, y), btnMask);
        dragTargetDragLeave();
        dragContext.stopDragging();
        this.onCursorChange(this, dragContext.getActualCursor());
    }

    public void cancelDrag() {
        dragTargetDragLeave();
        dragContext.stopDragging();
        this.onCursorChange(this, dragContext.getActualCursor());
    }

    // Closing
    public void close() {
        renderer.cleanup();
        cursorChangeListener.onCursorChange(0);
        super.close(true);
    }

    @Override
    protected void finalize() throws Throwable {
        renderer.cleanup();
        super.finalize();
    }

    // Cursor handling
    @Override
    public boolean onCursorChange(CefBrowser browser, int cursorType) {
        cursorType = dragContext.getVirtualCursor(cursorType);
        cursorChangeListener.onCursorChange(cursorType);
        return super.onCursorChange(browser, cursorType);
    }


    public void setCursor(CefCursorType cursorType) {
        var windowHandle = Display.getHandle();

        if (cursorType == CefCursorType.NONE) return;

        org.lwjgl.glfw.GLFW.glfwSetCursor(windowHandle, MCEFGlfwCursorHelper.getGLFWCursorHandle(cursorType));
    }

    /**
     * For some reason, middle and right are swapped in MC
     *
     * @param button the button to swap
     * @return the swapped button
     */
    private int swapButton(int button) {
        if (button == 1) {
            return 2;
        } else if (button == 2) {
            return 1;
        }

        return button;
    }
}