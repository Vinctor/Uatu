package com.vinctor.handler;

import com.android.build.api.transform.TransformInvocation;
import com.vinctor.Log;
import com.vinctor.UatuConfig;
import com.vinctor.UatuContext;
import com.vinctor.replace.ReplaceClassVisitor;
import com.vinctor.uatu.UatuClassVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class TransformHandler extends BaseHanlder {
    private final UatuConfig config;
    private final UatuContext context;

    public TransformHandler(TransformInvocation transformInvocation, UatuConfig config, UatuContext context) {
        super(transformInvocation);
        this.config = config;
        this.context = context;
    }

    @Override
    byte[] onHanlerFileInput(byte[] bytes) {
        return handlerClass(bytes);
    }

    @Override
    byte[] onHanlerJarInput(byte[] bytes) {
        return handlerClass(bytes);
    }


    byte[] handlerClass(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor upstreamCv = cw;
        Log.i("chain:" + upstreamCv);
        if (config.getTraceConfig() != null && config.getTraceConfig().enable) {
            ClassVisitor traceCv = new UatuClassVisitor(upstreamCv, context);
            upstreamCv = traceCv;
        }
        Log.i("chain:" + upstreamCv);
        if (config.getReplaceConfig() != null && config.getReplaceConfig().isEnable()) {
            ClassVisitor replaceCv = new ReplaceClassVisitor(upstreamCv, context);
            upstreamCv = replaceCv;
        }
        Log.i("chain:" + upstreamCv);
        cr.accept(upstreamCv, ClassReader.EXPAND_FRAMES);

        byte[] code = cw.toByteArray();
        return code;
    }
}
