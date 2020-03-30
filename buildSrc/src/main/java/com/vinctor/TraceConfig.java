package com.vinctor;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class TraceConfig {
    public TraceConfig() {
    }

    public boolean enable = false;
    public boolean logAllArgs = false;
    public String traceClass;
    public List<String> tracePackage;
    public List<String> exclude;

    public boolean isEnable() {
        boolean isTraceClassEmpty = TextUtils.isEmpty(traceClass);
        boolean isTracePackageEmpty = tracePackage == null || tracePackage.size() == 0;
        return enable && !isTraceClassEmpty && !isTracePackageEmpty;
    }

    public void init(boolean enable) {
        if (!enable) {
            this.enable = false;
        }
        boolean isEmpty = TextUtils.isEmpty(traceClass);
        if (!isEmpty) {
            traceClass = traceClass.replace(".", "/");
        }
        if (tracePackage != null) {
            tracePackage.replaceAll(new UnaryOperator<String>() {
                @Override
                public String apply(String item) {
                    return Utils.transformClassName(item);
                }
            });
        }
        if (exclude == null) {
            exclude = new ArrayList<>();
        }
        exclude.add(traceClass);
        exclude.replaceAll(new UnaryOperator<String>() {
            @Override
            public String apply(String item) {
                return Utils.transformClassName(item);
            }
        });
    }

    public void addReplaceExcluedClass(List<String> replaceExcluedClass) {
        if (exclude == null) {
            exclude = new ArrayList<>();
        }
        exclude.addAll(replaceExcluedClass);
    }

    @Override
    public String toString() {
        return "TraceConfig{" +
                "enable=" + enable +
                ", logAllArgs=" + logAllArgs +
                ", traceClass='" + traceClass + '\'' +
                ", tracePackage=" + tracePackage +
                ", exclude=" + exclude +
                '}';
    }
}
