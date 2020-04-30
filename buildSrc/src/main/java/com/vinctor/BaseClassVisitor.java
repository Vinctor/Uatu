package com.vinctor;

import org.objectweb.asm.ClassVisitor;

public class BaseClassVisitor extends ClassVisitor {
    UatuContext context;
    protected String className;

    public BaseClassVisitor(int api, UatuContext context) {
        super(api);
        this.context = context;
    }

    public BaseClassVisitor(int api, ClassVisitor cv, UatuContext context) {
        super(api, cv);
        this.context = context;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
