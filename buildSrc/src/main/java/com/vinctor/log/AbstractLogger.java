package com.vinctor.log;

import org.gradle.api.logging.LogLevel;

abstract class AbstractLogger implements ILogger {
    @Override
    public void d(String tag, String msg) {
        log(LogLevel.DEBUG, tag, msg, null);
    }

    @Override
    public void i(String tag, String msg) {
        log(LogLevel.INFO, tag, msg, null);
    }

    @Override
    public void w(String tag, String msg) {
        w(tag, msg, null);
    }

    @Override
    public void w(String tag, String msg, Throwable t) {
        log(LogLevel.WARN, tag, msg, t);
    }

    @Override
    public void e(String tag, String msg) {
        e(tag, msg, null);
    }

    @Override
    public void e(String tag, String msg, Throwable t) {
        log(LogLevel.ERROR, tag, msg, t);
    }

    protected abstract void log(LogLevel level, String tag, String msg, Throwable t);
}
