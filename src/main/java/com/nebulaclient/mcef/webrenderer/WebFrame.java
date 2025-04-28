package com.nebulaclient.mcef.webrenderer;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;

public class WebFrame {

    @Getter
    private double x,y,width,height;
    @Getter
    private boolean fullScreen;

    public WebFrame(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fullScreen = false;
    }

    public WebFrame() {
        this.x = 0;
        this.y = 0;
        this.width = MinecraftClient.getInstance().width;
        this.height = MinecraftClient.getInstance().height;
        fullScreen = true;
    }

    public double parseMouseX(double x) {
        return x - this.x;
    }

    public double parseMouseY(double y) {
        return y - this.y;
    }

}
