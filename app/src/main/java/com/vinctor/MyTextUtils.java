package com.vinctor;

import android.text.TextUtils;
import android.util.Log;

public class MyTextUtils {
    public static boolean isEmpty(CharSequence str) {
        Log.v("MyTextUtils", "CharSequence is:" + str);
        return TextUtils.isEmpty(str);
    }
}
