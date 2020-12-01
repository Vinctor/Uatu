package com.vinctor.plugin.uatulib.trace;

import android.os.Looper;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultUatuTrace implements ITraceListener {
    public static final String TAG = "UatuTraceDefaultLog";
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
    public String start(final String className, final String method, final String signature, Object[] args) {
        long startTs = System.currentTimeMillis();
        String id = UUID.randomUUID().toString();
        tsMap.put(id, startTs);
        Thread thread = Thread.currentThread();
        boolean isMainLooper = Looper.myLooper() == Looper.getMainLooper();
        Log.d(TAG, getStartLog(className, method, signature, args, thread, isMainLooper));
        return id;
    }

    public void end(String id, final String className, final String method, final String signature, final Object returnObj) {
        long endTs = System.currentTimeMillis();
        Long startTs = tsMap.get(id);
        long cost = 0;
        if (startTs != null) {
            cost = endTs - startTs;
        }
        tsMap.remove(id);
        Log.d(TAG, getEndLog(className, method, cost, returnObj));
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
}
