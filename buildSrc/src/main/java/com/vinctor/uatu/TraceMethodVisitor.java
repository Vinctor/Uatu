package com.vinctor.uatu;

import com.vinctor.TraceConfig;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;


public class TraceMethodVisitor extends AdviceAdapter {


    public final static String OBJECT_SIGNATURE = Type.getDescriptor(Object.class);
    private final static String OBJECT_ARRAY_SIGNATURE = "[Ljava/lang/Object;";

    private final String className;
    private final String desc;
    private final String methodName;
    private final TraceConfig config;
    private final String traceClass;
    private final Type[] argumentArrays;
    private final boolean isStatic;
    String stringDesc = Type.getType(String.class).getDescriptor();
    private int methodIdLocalIndex;
    private boolean isLog;

    protected TraceMethodVisitor(int api, MethodVisitor mv, String className, int access, String name, String desc, TraceConfig config) {
        super(api, mv, access, name, desc);
        this.className = className;
        this.methodName = name;
        this.desc = desc;
        this.config = config;
        this.isLog = config.logAllArgs;
        this.traceClass = config.traceClass;
        this.argumentArrays = Type.getArgumentTypes(desc);
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        if (isLog) {
            this.isLog = true;
            return super.visitAnnotation(s, b);
        }
        if ("Lcom/vinctor/plugin/uatulib/TraceLog;".equals(s)) {
            this.isLog = true;
            return null;
        }
        return super.visitAnnotation(s, b);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    protected void onMethodEnter() {

        methodIdLocalIndex = newLocal(Type.getType(String.class));
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitLdcInsn(getParseSignature());

        //args
        if (isLog) {
            loadArgArray();
        } else {
            push(0);
            newArray(Type.getType(Object.class));
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                traceClass,
                "start",
                "(" + stringDesc + stringDesc + stringDesc + OBJECT_ARRAY_SIGNATURE + ")" + stringDesc,
                false);
        storeLocal(methodIdLocalIndex);
    }

    private String getParseSignature() {
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        StringBuilder sb = new StringBuilder();
        int length = argumentTypes.length;
        for (int i = 0; i < length; i++) {
            String argClassName = argumentTypes[i].getClassName();
            sb.append(argClassName);
            if (i != length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }


    @Override
    protected void onMethodExit(int opcode) {
        //return
        int returnLocalIndex = newLocal(Type.getType(Object.class));
        if (isLog) {
            if (opcode == RETURN) {
                visitInsn(ACONST_NULL);
            } else if (opcode == ARETURN || opcode == ATHROW) {
                dup();
            } else {
                if (opcode == LRETURN || opcode == DRETURN) {
                    dup2();
                } else {
                    dup();
                }
                box(Type.getReturnType(methodDesc));
            }
        } else {
            visitInsn(ACONST_NULL);
        }
        storeLocal(returnLocalIndex);

        loadLocal(methodIdLocalIndex);
        mv.visitLdcInsn(className);
        mv.visitLdcInsn(methodName);
        mv.visitLdcInsn(getParseSignature());
        loadLocal(returnLocalIndex);

        mv.visitMethodInsn(
                INVOKESTATIC,
                traceClass,
                "end",
                "(" + stringDesc + stringDesc + stringDesc + stringDesc + OBJECT_SIGNATURE + ")V",
                false);
    }
}
