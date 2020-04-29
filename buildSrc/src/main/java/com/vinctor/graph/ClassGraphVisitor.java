package com.vinctor.graph;

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
        classNode.setParent(superName).setInterfaces(interfaces);
    }
}
