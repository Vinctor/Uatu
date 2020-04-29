package com.vinctor.handler;

import com.android.build.api.transform.TransformInvocation;
import com.vinctor.Log;
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


    byte[] handlerClass(byte[] bytes, boolean isJar) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor upstreamCv = cw;
        Log.i("chain:" + upstreamCv);
        TraceConfig traceConfig = config.getTraceConfig();
        boolean isTraceAllowjar = isJar ? (traceConfig.isJarEnable()) : true;
        if (traceConfig != null && traceConfig.isEnable() && isTraceAllowjar) {
            ClassVisitor traceCv = new UatuClassVisitor(upstreamCv, context);
            upstreamCv = traceCv;
        }
        Log.i("chain:" + upstreamCv);
        ReplaceConfig replaceConfig = config.getReplaceConfig();
        boolean isReplaceAllowjar = isJar ? (replaceConfig.isJarEnable()) : true;
        if (replaceConfig != null && replaceConfig.isEnable() && isReplaceAllowjar) {
            ClassVisitor replaceCv = new ReplaceClassVisitor(upstreamCv, context);
            upstreamCv = replaceCv;
        }
        Log.i("chain:" + upstreamCv);
        cr.accept(upstreamCv, ClassReader.EXPAND_FRAMES);

        byte[] code = cw.toByteArray();
        return code;
    }
}
