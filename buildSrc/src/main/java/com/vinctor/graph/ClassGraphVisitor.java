package com.vinctor.graph;

import com.vinctor.util.TypeUtil;

import org.objectweb.asm.ClassVisitor;

public class ClassGraphVisitor extends ClassVisitor {
    public ClassGraphVisitor(int api) {
        super(api);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        name = ClassNode.parseClassName(name);
        superName = ClassNode.parseClassName(superName);
        ClassNode classNode = ClassNodeGraph.getSingleton().getClassNode(name);
        if (classNode == null) {
            classNode = new ClassNode(name);
        }
        boolean isAbs = TypeUtil.isAbstract(access);
        boolean isInterface = TypeUtil.isInterface(access);
        classNode.setAbs(isAbs).setInterface(isInterface).setParent(superName).setInterfaces(interfaces);
    }
}
