package com.nebulaclient.mcef.webrenderer;

public interface IWebTab {


    void reload();
    void goForward();
    void goBack();

    void resize(int width, int height);


    boolean visible();
    WebFrame position();

}
