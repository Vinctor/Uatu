package com.vinctor.handler;

import com.android.build.api.transform.TransformInvocation;
import com.vinctor.graph.ClassGraphVisitor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public class PreStatisticsHandler extends BaseHanlder {
    public PreStatisticsHandler(TransformInvocation transformInvocation) {
        super(transformInvocation);
    }

    public static void start(TransformInvocation transformInvocation) {
        new PreStatisticsHandler(transformInvocation).start();
    }

    @Override
    byte[] onHanlerFileInput(byte[] bytes) {
        statistics(bytes);
        return bytes;
    }

    private void statistics(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassGraphVisitor cv = new ClassGraphVisitor(Opcodes.ASM5);
        cr.accept(cv, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }

    @Override
    byte[] onHanlerJarInput(byte[] bytes) {
        statistics(bytes);
        return bytes;
    }
}
