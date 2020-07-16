package com.vinctor.log;

import org.gradle.api.Project;
import org.gradle.api.logging.Logging;

public class Log {


    public final static String TAG = "UATU";
    private static ILogger logger;

    static {
        logger = new GradleBuildLogger(Logging.getLogger("uatu"));
    }

    public static void init(Project project) {
        logger = new GradleBuildLogger(project);
    }

    public static void i(Object msg) {
        logger.i(TAG, msg.toString());
    }

    public static void i(String tag, Object msg) {
        logger.i(tag, "-->" + msg);
    }

    public static void e(String tag, Object msg) {
        logger.e(tag, "-->" + msg);
    }

    public static void e(Object msg) {
        logger.e(TAG, "-->" + msg);
    }
}
