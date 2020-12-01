package com.vinctor.trace;

import com.vinctor.Utils;
import com.vinctor.graph.ClassNode;
import com.vinctor.graph.ClassNodeGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public class TraceConfig {
    public TraceConfig() {
    }

    public boolean enable = false;
    public boolean jarEnable = false;
    public boolean logAllArgs = false;
    public List<String> tracePackage;
    public List<String> exclude;

    private String traceClass;
    private Set<String> excludeSet = new HashSet<>();

    public boolean isEnable() {
        boolean isTracePackageEmpty = tracePackage == null || tracePackage.size() == 0;
        return enable && !isTracePackageEmpty;
    }

    public void init(boolean enable) {
        if (!enable) {
            this.enable = false;
            return;
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
        exclude.forEach(item -> {
            String className = Utils.transformClassName(item);
            excludeSet.add(className);
        });
    }

    public Set<String> getExcludeSet() {
        return excludeSet;
    }

    public void setTraceClass(String traceClass) {
        this.traceClass = traceClass;
        excludeSet.add(traceClass);
    }

    public String getTraceClass() {
        return traceClass;
    }

    public boolean isJarEnable() {
        return jarEnable;
    }

    public void addReplaceExcluedClasses(List<String> replaceExcluedClassList) {
        excludeSet.addAll(replaceExcluedClassList);
    }

    public void addReplaceExcluedClass(String replaceExcluedClass) {
        excludeSet.add(replaceExcluedClass);
    }

    @Override
    public String toString() {
        return "TraceConfig{" +
                "enable=" + enable +
                ", jarEnable=" + jarEnable +
                ", logAllArgs=" + logAllArgs +
                ", traceClass='" + traceClass + '\'' +
                ", tracePackage=" + tracePackage +
                ", exclude=" + excludeSet +
                '}';
    }

    public static final String TRACE_CLASS_INTERFACE = "com.vinctor.plugin.uatulib.trace.ITraceListener".replace(".", "/");
    public static final String DEFAULT_TRACE_CLASS = ".com.vinctor.plugin.uatulib.trace.DefaultUatuTrace".replace(".", "/");
    public static final String TRACE_UTIL_CLASS_NAME = "com.vinctor.plugin.uatulib.trace.TraceUtil".replace(".", "/");
    public static final String GET_TRACE_CLASS_INTANCE_METHOD_NAME = "getIntance";

    public static String getTraceClassFromGraph(TraceConfig traceConfig) {
        ClassNode classNode = ClassNodeGraph.getSingleton().getClassNode(TRACE_CLASS_INTERFACE);
        String result = DEFAULT_TRACE_CLASS;
        if (classNode != null) {
            Set<ClassNode> allLeafChilren = classNode.getAllLeafChilren();
            for (ClassNode node : allLeafChilren) {
                if (node.isAbs() || node.isInterface()) {
                    continue;
                }
                if (node.getName().equals(DEFAULT_TRACE_CLASS)) {
                    continue;
                }
                result = node.getName();
                while (node != null) {
                    String name = node.getName();
                    traceConfig.addReplaceExcluedClass(name);
                    node = node.getParent();
                }
                break;
            }
        }
        return result;
    }
}
