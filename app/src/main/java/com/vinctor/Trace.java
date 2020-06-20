package com.vinctor;

import android.app.Activity;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.vinctor.Uatu.BaseView;
import com.vinctor.Uatu.MainActivity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Trace {
    static ConcurrentHashMap<String, Long> tsMap = new ConcurrentHashMap();

    /**
     * 当一个方法进入时会调用
     *
     * @param className 被跟踪的类名
     * @param method    被跟踪的方法名
     * @param signature 被跟踪的方法描述
     * @param args      被跟踪的方法参数
     * @return 用来识别方法唯一标识, randomUUID, 或int累加
     */
    public static String start(final String className, final String method, final String signature, Object[] args) {
        long startTs = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        tsMap.put(id, startTs);
        Thread thread = Thread.currentThread();
        boolean isMainLooper = Looper.myLooper() == Looper.getMainLooper();
        Log.d("MethodTrace_start", getStartLog(className, method, signature, args, thread, isMainLooper));
        return id;
    }

    public static void end(String id, final String className, final String method, final String signature, final Object returnObj) {
        long endTs = System.currentTimeMillis();
        Long startTs = tsMap.get(id);
        long cost = 0;
        if (startTs != null) {
            cost = endTs - startTs;
        }
        tsMap.remove(id);
        Log.d("MethodTrace_end", getEndLog(className, method, cost, returnObj));
    }

    private static String getEndLog(String className, String method, long cost, Object returnObj) {
        return getSimpleName(className) + "#" + method + "-->(" + returnObj + "), cost:" + cost + "ms";
    }


    private static String getStartLog(String className, String method, String signature, Object[] args, Thread thread, boolean isMainLooper) {
        return getSimpleName(className) + "#" + method + "(thread:" + thread.getName() + ",mainLooper:" + isMainLooper + ")" + "-->(" + getArgs(args) + ")";
    }

    private static String getSimpleName(String className) {
        String[] split = className.split("/");
        return split[split.length - 1];
    }


    private static String getArgs(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg == null ? "null" : arg.toString());
            sb.append(",");
        }
        return sb.toString();
    }

    public static void log(MainActivity activity, String msg) {
        activity.log(msg);
    }

    public static void printStackTrace(Throwable throwable) {
        throwable.printStackTrace();
    }

    public static void onCreate(BaseView baseView) {
        baseView.onCreate();
    }

    public static boolean isCorrect(BaseView baseView) {
        return baseView.isCorrect();
    }

    public static Trace getTrace(BaseView baseView, MyTextUtils myTextUtils) {
        return null;
    }

    void getArray(BaseView baseView, String[][] a) {

    }
}
