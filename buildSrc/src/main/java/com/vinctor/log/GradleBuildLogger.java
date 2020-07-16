package com.vinctor.log;

import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;

class GradleBuildLogger extends AbstractLogger {


    private Logger logger;

    public GradleBuildLogger(Project project) {
        logger = project.getLogger();
    }

    public GradleBuildLogger(Logger logger) {
       this.logger=logger;
    }

    @Override
    protected void log(LogLevel level, String tag, String msg, Throwable t) {
        if (t != null) {
            logger.log(level, String.format("[%s] %s", tag, msg), t);
        } else {
            logger.log(level, String.format("[%s] %s", tag, msg));
        }
    }
}
