package com.vinctor;

import org.objectweb.asm.ClassVisitor;

public class BaseClassVisitor extends ClassVisitor {
    UatuContext context;

    public BaseClassVisitor(int api, UatuContext context) {
        super(api);
        this.context = context;
    }

    public BaseClassVisitor(int api, ClassVisitor cv, UatuContext context) {
        super(api, cv);
        this.context = context;
    }
}
