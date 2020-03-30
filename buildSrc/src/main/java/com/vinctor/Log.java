package com.vinctor;

public class Log {
    static boolean Debug = false;

    public static void i(Object msg) {
        if (Debug) {
            System.out.println(msg);
        }
    }

    public static void i(String tag, Object msg) {
        i(tag + "-->" + msg);
    }

    public static void e(Object msg) {
        System.out.println(msg);
    }

    public static void e(String tag, Object msg) {
        e(tag + "-->" + msg);
    }
}
