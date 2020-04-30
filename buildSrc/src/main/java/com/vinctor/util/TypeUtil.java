package com.vinctor.util;

import org.objectweb.asm.Opcodes;

public class TypeUtil {

    public static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
    }

    public static boolean isAbstract(int access) {
        return (access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;
    }

    public static boolean isInterface(int access) {
        return (access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
    }

    public static boolean isSynthetic(int access) {
        return (access & Opcodes.ACC_SYNTHETIC) == Opcodes.ACC_SYNTHETIC;
    }

    public static boolean isPrivate(int access) {
        return (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE;
    }

    public static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
    }

    public static int resetAccessScope(int access, int scope) {
        return access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED) | scope;
    }
}
