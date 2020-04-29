package com.vinctor.replace;

import com.vinctor.Log;
import com.vinctor.graph.ClassNode;
import com.vinctor.graph.ClassNodeGraph;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.AccessibleObject;
import java.util.List;
import java.util.Set;

public class ReplaceMethodVisitor extends GeneratorAdapter {

    private final ReplaceConfig config;
    private final List<MethodNode> methodNodes;
    int count = 1;

    public ReplaceMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc, ReplaceConfig config, List<MethodNode> methodNodes) {
        super(api, mv, access, name, desc);
        this.config = config;
        this.methodNodes = methodNodes;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        List<ReplaceBean> replaceList = config.getReplaceList();
        Log.i("visitMethodInsn", opcode + "-" + name);
        for (ReplaceBean replaceBean : replaceList) {
            ReplaceBean.methodBean from = replaceBean.from;
            ReplaceBean.methodBean to = replaceBean.to;
            if (name.equals(from.methodName) && desc.equals(from.methodDesc) && handlerMethod(opcode, owner, name, desc, from, to)) {
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    boolean handlerMethod(int opcode, String originClassName, String originMethodName, String desc, ReplaceBean.methodBean from, ReplaceBean.methodBean to) {
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
                    //generateLeafMothod(opcode, originClassName, originMethodName, desc);
                } else {
                    //not leaf, jump
                }
            } else {
                //not found, generate mothod

            }
        }


        //all
        if (scopeTypeInt == Constants.Scope.SCOPE_ALL_TYPE) {

        }

        return false;
    }
}
