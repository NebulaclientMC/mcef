package com.nebulaclient.mcef.mixins;

import com.nebulaclient.mcef.ExampleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract void setScreen(Screen screen);

    @Shadow public Screen currentScreen;

    @Shadow private static MinecraftClient instance;

    @Shadow public GameOptions options;

    @Inject(method = "initializeGame",at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        System.setProperty("java.awt.headless", "true");
    }

    @Inject(method = "handleKeyInput",at = @At("RETURN"))
    public void init2(CallbackInfo ci) {
        if (Keyboard.getEventKey() == Keyboard.KEY_I && currentScreen == null) {
            setScreen(new ExampleScreen());
        }
    }

    /**
     * TODO: Look at this if performance doesn't seem good!
     * Give ExampleScreen.java a max cap of 200 fps
     */
    /**@Overwrite
     *
        public int getMaxFramerate() {
            return MinecraftClient.getInstance().currentScreen instanceof ExampleScreen ? 200 : MinecraftClient.getInstance().currentScreen == null ? options.maxFramerate : 60;
        }
     **/




}
