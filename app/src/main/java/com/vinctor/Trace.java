package com.vinctor;

import android.text.TextUtils;
import android.util.Log;

import com.vinctor.Uatu.MainActivity;

import java.util.UUID;

public class Trace {
    public static String start(final String className, final String method, final String signature, Object[] args) {
        long startTs = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        Log.d("MethodTrace", className + "--" + method);
        return id;
    }


    public static void end(String id, final String className, final String method, final String signature, final Object returnObj) {

    }

    static void log(MainActivity activity, String msg) {
        activity.log(msg);
    }

    static boolean isEmpty(CharSequence str) {
        return TextUtils.isEmpty(str);
    }
}
