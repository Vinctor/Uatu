package com.vinctor.trace;

import com.vinctor.BaseClassVisitor;
import com.vinctor.log.Log;
import com.vinctor.UatuContext;
import com.vinctor.util.TypeUtil;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.List;
import java.util.Set;

public class UatuClassVisitor extends BaseClassVisitor {
    public static final String TRACT_INTERFACE_DESC = "L" + TraceConfig.TRACE_CLASS_INTERFACE + ";";
    public static final String TRACT_GET_INTANCT_METHOD_DESC = "()" + TRACT_INTERFACE_DESC;

    private final TraceConfig config;
    private final boolean isClassInJar;
    private final boolean isJarEnable;
    private String className;
    private boolean isAbs = false;
    private boolean isTrace = false;
    private boolean isSpecialTraceUtilClass = false;
    private boolean isHasGenerateInstanceMethod = false;

    public UatuClassVisitor(ClassVisitor cv, UatuContext context, boolean isClassInJar) {
        super(Opcodes.ASM6, cv, context);
        this.config = context.getConfig().getTraceConfig();
        this.isClassInJar = isClassInJar;
        isJarEnable = config.isJarEnable();
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

        Set<String> excludeSet = config.getExcludeSet();
        if (excludeSet != null) {
            for (String s : excludeSet) {
                if (!"".equals(s) && className.contains(s)) {
                    isTrace = false;
                    break;
                }
            }
        }

        if (TypeUtil.isAbstract(access) || TypeUtil.isInterface(access)) {
            this.isAbs = true;
        }

        if (TraceConfig.TRACE_UTIL_CLASS_NAME.equals(name)) {
            isSpecialTraceUtilClass = true;
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (TraceConfig.GET_TRACE_CLASS_INTANCE_METHOD_NAME.equals(name)
                && TypeUtil.isStatic(access)
                && TypeUtil.isPublic(access)
                && TRACT_GET_INTANCT_METHOD_DESC.equals(desc)
        ) {
            isHasGenerateInstanceMethod = true;
        }
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        boolean isConstrctor = name.contains("<init>") ? true : false;
        boolean isClassInit = name.contains("<clinit>") ? true : false;
        boolean isInnerClassGenerateMethod = name.startsWith("access$");
        if (isSpecialTraceUtilClass) {
            //特殊 生成getIntance方法
            return methodVisitor;
        }
        if (isClassInJar && !isJarEnable) {
            return methodVisitor;
        }
        if (!isTrace || isAbs || isConstrctor || isClassInit || isInnerClassGenerateMethod) {
            return methodVisitor;
        }
        Log.i(className + "--" + name + " is changing...");
        return new TraceMethodVisitor(Opcodes.ASM6, methodVisitor, className, access, name, desc, config);
    }

    @Override
    public void visitEnd() {
        if (isSpecialTraceUtilClass && !isHasGenerateInstanceMethod) {
            Method method = new Method(TraceConfig.GET_TRACE_CLASS_INTANCE_METHOD_NAME, Type.getReturnType(TRACT_GET_INTANCT_METHOD_DESC), Type.getArgumentTypes(TRACT_GET_INTANCT_METHOD_DESC));
            GeneratorAdapter methodVisitor = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, method, null, null, cv);
            genarateIntanceMethod(methodVisitor);
        }
        super.visitEnd();
    }

    private void genarateIntanceMethod(GeneratorAdapter methodVisitor) {
        methodVisitor.visitCode();
        Type traceClassType = Type.getObjectType(config.getTraceClass());
        Method constructorMethod = Method.getMethod("void <init> ()");
        methodVisitor.newInstance(traceClassType);
        methodVisitor.dup();
        methodVisitor.invokeConstructor(traceClassType, constructorMethod);
        methodVisitor.returnValue();
        methodVisitor.endMethod();
    }
}
