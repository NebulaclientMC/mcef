package com.nebulaclient.mcef.mixins;

import com.nebulaclient.mcef.ExampleScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "initializeGame",at = @At("HEAD"))
    public void init(CallbackInfo ci) {
        System.setProperty("java.awt.headless", "true"); //Do not remove
    }
    boolean toggeled = false;

    @Inject(method = "handleKeyInput",at = @At("RETURN"))
    public void init2(CallbackInfo ci) {
            MinecraftClient.getInstance().setScreen(new ExampleScreen());

    }

    @Overwrite
    public int getMaxFramerate() {
        return 200;
    }




}
