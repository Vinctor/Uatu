package com.vinctor.trace;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;


public class TraceMethodVisitor extends AdviceAdapter {


    public final static String OBJECT_SIGNATURE = Type.getDescriptor(Object.class);
    static Type ARRAY_OBJ_TYPE = Type.getType(Object[].class);
    static Type OBJ_TYPE = Type.getType(Object.class);
    static Type STRING_TYPE = Type.getType(String.class);


    static Type TRACT_CLASS_TYPE = Type.getObjectType(TraceConfig.TRACE_CLASS_INTERFACE);
    static Type TRACT_UTIL_CLASS_TYPE = Type.getObjectType(TraceConfig.TRACE_UTIL_CLASS_NAME);
    static Method METHOD_GET_INTANCE = Method.getMethod(TraceConfig.TRACE_CLASS_INTERFACE.replace('/', '.') + " " + TraceConfig.GET_TRACE_CLASS_INTANCE_METHOD_NAME + "()");
    static Method METHOD_TRACE_START = new Method("start", STRING_TYPE, new Type[]{STRING_TYPE, STRING_TYPE, STRING_TYPE, ARRAY_OBJ_TYPE});
    static Method METHOD_TRACE_END = new Method("end", Type.VOID_TYPE, new Type[]{STRING_TYPE, STRING_TYPE, STRING_TYPE, STRING_TYPE, OBJ_TYPE});

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
    private int traceClassLocalIndex;

    protected TraceMethodVisitor(int api, MethodVisitor mv, String className, int access, String name, String desc, TraceConfig config) {
        super(api, mv, access, name, desc);
        this.className = className;
        this.methodName = name;
        this.desc = desc;
        this.config = config;
        this.isLog = config.logAllArgs;
        this.traceClass = config.getTraceClass();
        this.argumentArrays = Type.getArgumentTypes(desc);
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        if (isLog) {
            this.isLog = true;
            return super.visitAnnotation(s, b);
        }
        return super.visitAnnotation(s, b);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    protected void onMethodEnter() {
        //get trace class
        invokeStatic(TRACT_UTIL_CLASS_TYPE, METHOD_GET_INTANCE);
        traceClassLocalIndex = newLocal(TRACT_CLASS_TYPE);
        storeLocal(traceClassLocalIndex);
        loadLocal(traceClassLocalIndex);

        //start method
        push(className);
        push(methodName);
        push(getParseSignature());
        //args
        if (isLog) {
            loadArgArray();
        } else {
            push(0);
            newArray(Type.getType(Object.class));
        }
        methodIdLocalIndex = newLocal(Type.getType(String.class));
        invokeInterface(TRACT_CLASS_TYPE, METHOD_TRACE_START);
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

        //load trace class
        loadLocal(traceClassLocalIndex);
        //load method id
        loadLocal(methodIdLocalIndex);
        push(className);
        push(methodName);
        push(getParseSignature());
        loadLocal(returnLocalIndex);

        invokeInterface(TRACT_CLASS_TYPE, METHOD_TRACE_END);
    }
}
