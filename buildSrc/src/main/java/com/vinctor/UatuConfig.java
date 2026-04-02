package com.vinctor;

import com.vinctor.trace.TraceConfig;

import org.gradle.api.Project;

public class UatuConfig {

    boolean enable = false;

    public TraceConfig traceConfig;

    public UatuConfig(Project project) {
        project.getExtensions().create("traceConfig", TraceConfig.class);
    }

    public void init() {
        if (traceConfig != null) {
            traceConfig.addExcludedClass(TraceConfig.DEFAULT_TRACE_CLASS);
            traceConfig.init(enable);
            enable = traceConfig.isEnable();
        }
    }

    public TraceConfig getTraceConfig() {
        return traceConfig;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "UatuConfig{\n" +
                "enable=" + enable +
                ",\ntraceConfig=" + traceConfig +
                "\n}";
    }
}
