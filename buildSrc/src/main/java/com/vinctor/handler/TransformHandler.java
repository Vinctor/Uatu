package com.vinctor.handler;

import com.android.build.api.transform.TransformInvocation;
import com.vinctor.replace.ReplaceConfig;
import com.vinctor.trace.TraceConfig;
import com.vinctor.UatuConfig;
import com.vinctor.UatuContext;
import com.vinctor.replace.ReplaceClassVisitor;
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
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor upstreamCv = cw;
        TraceConfig traceConfig = config.getTraceConfig();
        if (traceConfig != null && traceConfig.isEnable()) {
            ClassVisitor traceCv = new UatuClassVisitor(upstreamCv, context,isClassInJar);
            upstreamCv = traceCv;
        }
        ReplaceConfig replaceConfig = config.getReplaceConfig();
        boolean isReplaceAllowjar = isClassInJar ? (replaceConfig.isJarEnable()) : true;
        if (replaceConfig != null && replaceConfig.isEnable() && isReplaceAllowjar) {
            ClassVisitor replaceCv = new ReplaceClassVisitor(upstreamCv, context);
            upstreamCv = replaceCv;
        }
        cr.accept(upstreamCv, ClassReader.EXPAND_FRAMES);

        byte[] code = cw.toByteArray();
        return code;
    }
}
