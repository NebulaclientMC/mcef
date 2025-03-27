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

import com.nebulaclient.mcef.messaging.QueryMessageManager;
import com.nebulaclient.mcef.examples.messaging.ExampleMessage;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class MCEFExampleMod {
    private static final MinecraftClient minecraft = MinecraftClient.getInstance();

    public static final KeyBinding KEY_MAPPING = new KeyBinding(
            "Open Browser",
            GLFW.GLFW_KEY_I, "key.categories.misc"
    );

    public MCEFExampleMod() {

        ClientTickEvents.START_CLIENT_TICK.register((client) -> onTick());
        QueryMessageManager.registerMessage(new ExampleMessage());
    }

    public void onTick() {
        // Check if our key was pressed
        if (KEY_MAPPING.isPressed() && !(minecraft.currentScreen instanceof ExampleScreen)) {
            //Display the web browser UI.
            minecraft.setScreen(new ExampleScreen());
        }
    }
}
