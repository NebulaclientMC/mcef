package com.nebulaclient.mcef.mixins;

import com.nebulaclient.mcef.ExampleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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

    @Inject(method = "initializeGame",at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        //Looks like every mod needs to do this theirself
        System.setProperty("java.awt.headless", "true");
    }

    @Inject(method = "handleKeyInput",at = @At("RETURN"))
    public void init2(CallbackInfo ci) {
        if (Keyboard.getEventKey() == Keyboard.KEY_I && currentScreen == null) {
            setScreen(new ExampleScreen());
        }
    }

    @Overwrite
    public int getMaxFramerate() {
        return MinecraftClient.getInstance().currentScreen instanceof ExampleScreen ? 200 : 60;
    }




}
