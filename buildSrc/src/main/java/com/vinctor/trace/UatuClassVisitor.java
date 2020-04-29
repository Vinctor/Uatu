package com.vinctor.trace;

import com.vinctor.BaseClassVisitor;
import com.vinctor.Log;
import com.vinctor.UatuContext;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class UatuClassVisitor extends BaseClassVisitor {

    private final TraceConfig config;
    private String className;
    private boolean isAbs = false;
    private boolean isTrace = false;

    public UatuClassVisitor(ClassVisitor cv, UatuContext context) {
        super(Opcodes.ASM5, cv, context);
        this.config = context.getConfig().getTraceConfig();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;

        List<String> tractPackage = config.tracePackage;
        if (tractPackage == null || (tractPackage.size() == 1 && tractPackage.get(0).equals("*"))) {
            isTrace = true;
        } else {
            for (String s : tractPackage) {
                if (!"".equals(s) && className.contains(s)) {
                    isTrace = true;
                    break;
                }
            }
        }

        List<String> exclude = config.exclude;
        if (exclude != null) {
            for (String s : exclude) {
                if (!"".equals(s) && className.contains(s)) {
                    isTrace = false;
                    break;
                }
            }
        }

        if (access == Opcodes.ACC_ABSTRACT || access == Opcodes.ACC_INTERFACE) {
            this.isAbs = true;
        }

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        boolean isConstrctor = name.contains("<init>") ? true : false;
        boolean isClassInit = name.contains("<clinit>") ? true : false;
        boolean isInnerClassGenerateMethod = name.startsWith("access$");
        if (!isTrace || isAbs || isConstrctor || isClassInit || isInnerClassGenerateMethod) {
            return methodVisitor;
        }
        Log.i(className + "--" + name + " is changing...");
        return new TraceMethodVisitor(Opcodes.ASM5, methodVisitor, className, access, name, desc, config);
    }
}
