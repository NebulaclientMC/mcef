package com.nebulaclient.mcef.webrenderer;


public interface InputHandler {

    void mousePress(double x, double y, int button);
    void mouseRelease(double x, double y, int button);

    void mouseMove(double x, double y, int button);
    void mouseScrolled(double wheel);

    void keyPress(int keyCode, int scanCode, int modifiers);
    void keyRelease(int keyCode, int scanCode, int modifiers);

    void charTyped(char ch, int keyCode);

}
