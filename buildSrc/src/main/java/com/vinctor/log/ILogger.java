package com.vinctor.log;


public interface ILogger {

    void d(String tag, String msg);

    void i(String tag, String msg);

    void w(String tag, String msg);

    void w(String tag, String msg, Throwable t);

    void e(String tag, String msg);

    void e(String tag, String msg, Throwable t);
}
