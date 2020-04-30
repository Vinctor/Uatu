package com.vinctor.replace;

import com.vinctor.Log;
import com.vinctor.graph.ClassNode;
import com.vinctor.graph.ClassNodeGraph;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ReplaceMethodVisitor extends GeneratorAdapter {

    private final ReplaceConfig config;
    private final HashMap<String, GeneratorAdapter> methodNodes;
    private final ClassVisitor cv;
    static int count = 1;
    private final String className;

    public ReplaceMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc, ReplaceConfig config, HashMap<String, GeneratorAdapter> methodNodes, ClassVisitor cv, String className) {
        super(api, mv, access, name, desc);
        this.config = config;
        this.methodNodes = methodNodes;
        this.cv = cv;
        this.className = className;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        List<ReplaceBean> replaceList = config.getReplaceList();
        Log.i("visitMethodInsn", opcode + "-" + name);
        for (ReplaceBean replaceBean : replaceList) {
            ReplaceBean.methodBean from = replaceBean.from;
            ReplaceBean.methodBean to = replaceBean.to;
            if (name.equals(from.methodName) && desc.equals(from.methodDesc) && handlerMethod(opcode, owner, name, desc, from, to, itf)) {
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    boolean handlerMethod(int opcode, String originClassName, String originMethodName, String desc, ReplaceBean.methodBean from, ReplaceBean.methodBean to, boolean itf) {
        int scopeTypeInt = from.getScopeTypeInt();
        //self
        if (scopeTypeInt == Constants.Scope.SCOPE_SELF_TYPE && originClassName.equals(from.className)) {
            super.visitMethodInsn(Opcodes.INVOKESTATIC, to.className, to.methodName, to.methodDesc, false);
            return true;
        }
        //leaf
        if (scopeTypeInt == Constants.Scope.SCOPE_LEAF_TYPE) {
            ClassNode classNode = ClassNodeGraph.getClassNode(originClassName);
            if (classNode != null) {
                if (classNode.getChildren().size() == 0) {
                    Set<ClassNode> allParentAndInterfaces = classNode.findAllParentAndInterfaces();
                    for (ClassNode node : allParentAndInterfaces) {
                        if (node != null && from.className.equals(node.name)) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, to.className, to.methodName, to.methodDesc, false);
                            return true;
                        }
                    }
                    //not found, generate mothod
                    generateLeafMothod(opcode, originClassName, originMethodName, desc, from, to, itf);
                    return true;
                } else {
                    //not leaf, jump
                }
            } else {
                //not found, generate mothod
                generateLeafMothod(opcode, originClassName, originMethodName, desc, from, to, itf);
                return true;
            }
        }


        //all
        if (scopeTypeInt == Constants.Scope.SCOPE_ALL_TYPE) {
            generateLeafMothod(opcode, originClassName, originMethodName, desc, from, to, itf);
            return true;
        }

        return false;
    }

    private void generateLeafMothod(int opcode, String originMethodClassName, String originMethodName, String originDesc, ReplaceBean.methodBean from, ReplaceBean.methodBean to, boolean itf) {
        String methodKey = getMethodKey(opcode, originMethodClassName, originMethodName, originDesc);
        String methodName = getMethodName(originMethodClassName, originMethodName);
        GeneratorAdapter methodVisitor = methodNodes.get(methodKey);
        Type[] originArgTypes = Type.getArgumentTypes(originDesc);
        Type[] argTypes = originArgTypes;
        if (opcode != Opcodes.INVOKESTATIC) {
            argTypes = new Type[originArgTypes.length + 1];
            argTypes[0] = Type.getObjectType(originMethodClassName);
            for (int i = 0; i < originArgTypes.length; i++) {
                argTypes[i + 1] = originArgTypes[i];
            }
        }
        Method m = new Method(methodName, Type.getReturnType(originDesc), argTypes);
        if (methodVisitor == null) {
            methodVisitor = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, m, null, null, cv);
            methodVisitor.visitCode();
            methodVisitor.loadArg(0);
            methodVisitor.instanceOf(Type.getType(from.className));
            Label elseLabel = methodVisitor.newLabel();
            methodVisitor.ifZCmp(EQ, elseLabel);
            methodVisitor.loadArg(0);
            methodVisitor.invokeStatic(Type.getObjectType(to.className), new Method(to.methodName, to.methodDesc));
            Label labelContinue = methodVisitor.newLabel();
            methodVisitor.goTo(labelContinue);
            methodVisitor.mark(elseLabel);
            methodVisitor.loadArg(0);
            methodVisitor.visitMethodInsn(opcode, originMethodClassName, originMethodName, originDesc, itf);
            methodVisitor.mark(labelContinue);
            methodVisitor.returnValue();
            methodNodes.put(methodKey, methodVisitor);
        }
        invokeStatic(Type.getObjectType(className), m);
    }

    String getMethodKey(int opcode, String originClassName, String originMethodName, String desc) {
        return opcode + "_" + originClassName + "_" + originMethodName + "_" + desc;
    }

    String getMethodName(String originClassName, String originMethodName) {
        return originClassName.replace('/', '_') + "_" + originMethodName;
    }
}
