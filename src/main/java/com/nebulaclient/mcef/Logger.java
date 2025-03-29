package com.nebulaclient.mcef;

import java.io.IOException;

public class Logger {
    public void info(String message) {
        System.out.println(message);
    }

    public void error(String s, Object e) {
        System.err.println(s);
    }

    public void info(String s, Object osArch) {
        System.out.println(s + " " + osArch);
    }
    public void info(String s, Object... osArch) {
        System.out.println(s + " " + osArch);
    }


    public void error(Object... s) {
        System.err.println(s);
    }

    public void warn(Object s) {
    } public void warn(Object s,Object e) {
    }

}
