package com.nebulaclient.mcef.webrenderer;

public interface IWebRenderer
{

    void makeDependenciesAvailable();

    void start();

    void stop();

    boolean isInitialized();

    void render();


}
