package com.vinctor.handler;

import com.android.build.api.transform.TransformInvocation;
import com.vinctor.trace.TraceConfig;
import com.vinctor.UatuConfig;
import com.vinctor.UatuContext;
import com.vinctor.trace.UatuClassVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class TransformHandler extends BaseHanlder {
    private final UatuConfig config;
    private final UatuContext context;

    public static void start(TransformInvocation transformInvocation, UatuConfig config, UatuContext context) {
        new TransformHandler(transformInvocation, config, context).start();
    }

    public static void start(TransformInvocation transformInvocation, UatuConfig config,
                             UatuContext context, boolean incremental) {
        TransformHandler handler = new TransformHandler(transformInvocation, config, context);
        handler.setIncremental(incremental);
        handler.start();
    }

    public TransformHandler(TransformInvocation transformInvocation, UatuConfig config, UatuContext context) {
        super(transformInvocation);
        this.config = config;
        this.context = context;
    }

    @Override
    protected boolean isEnable() {
        return config.isEnable();
    }

    @Override
    byte[] onHanlerFileInput(byte[] bytes) {
        return handlerClass(bytes, false);
    }

    @Override
    byte[] onHanlerJarInput(byte[] bytes) {
        return handlerClass(bytes, true);
    }

    byte[] handlerClass(byte[] bytes, boolean isClassInJar) {
        TraceConfig traceConfig = config.getTraceConfig();
        // 不需要插桩时直接返回原始字节
        if (traceConfig == null || !traceConfig.isEnable()) {
            return bytes;
        }

        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new UatuClassVisitor(cw, context, isClassInJar);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
