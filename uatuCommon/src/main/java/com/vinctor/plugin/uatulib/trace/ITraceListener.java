package com.vinctor.plugin.uatulib.trace;

public interface ITraceListener {
    /**
     * 当一个方法进入时会调用
     *
     * @param className 被跟踪的类名
     * @param method    被跟踪的方法名
     * @param signature 被跟踪的方法描述
     * @param args      被跟踪的方法参数
     * @return 用来识别方法唯一标识, randomUUID, 或int累加
     */
    String start(final String className, final String method, final String signature, Object[] args);

    /**
     * 当一个方法即将退出时会调用
     *
     * @param id        用来识别方法唯一标识, 与start方法返回的id一致
     * @param className 被跟踪的类名
     * @param method    被跟踪的方法名
     * @param signature 被跟踪的方法描述
     * @param returnObj 方法返回值, 可能为null
     */
    void end(String id, final String className, final String method, final String signature, final Object returnObj);
}
