package com.vinctor.trace.room;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final ExecutorService executorService;

    private ThreadPool() {
        executorService = new ThreadPoolExecutor(
                Constant.corePoolSize,
                Constant.maximumPoolSize,
                Constant.keepAliveTime,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public static ThreadPool getSqlThreadPool() {
        return SingletonHolder.INSTANCE;
    }

    public void execTask(Runnable runnable) {
        executorService.execute(runnable);
    }

    private static class SingletonHolder {
        private static final ThreadPool INSTANCE = new ThreadPool();
    }
}
