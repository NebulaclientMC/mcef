package com.nebulaclient.mcef;

import java.io.IOException;

public class Logger {
    public void info(String message) {
        System.out.println(message);
    }

    public void error(String s, IOException e) {
        System.err.println(s);
    }

    public void info(String s, Object osArch) {
        System.out.println(s + " " + osArch);
    }

    public void error(String s) {
        System.err.println(s);
    }
}
