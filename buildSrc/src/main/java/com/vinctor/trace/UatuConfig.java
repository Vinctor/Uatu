package com.vinctor.trace;

import java.util.Arrays;
import java.util.List;

public class UatuConfig {
    boolean enable = false;
    boolean logAllArgs = false;
    String traceClass;
    String[] tracePackage;
    String[] exclude;

    public boolean isTraceClassEnable() {
        boolean isEmpty = traceClass == null || "".equals(traceClass);
        if (!isEmpty) {
            traceClass = traceClass.replace(".", "/");
        }
        return !isEmpty;
    }

    public List<String> getTracePackage() {
        return tracePackage == null ? null : Arrays.asList(tracePackage);
    }

    public List<String> getExclude() {
        return exclude == null ? null : Arrays.asList(exclude);
    }

    @Override
    public String toString() {
        return "UatuConfig{" +
                "enable=" + enable +
                ", logAllArgs=" + logAllArgs +
                ", traceClass='" + traceClass + '\'' +
                ", tracePackage=" + Arrays.toString(tracePackage) +
                ", exclude=" + Arrays.toString(exclude) +
                '}';
    }
}
