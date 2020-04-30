package com.vinctor.replace;

import com.vinctor.BaseClassVisitor;
import com.vinctor.UatuContext;
import com.vinctor.util.TypeUtil;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.Hash;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;


public class ReplaceClassVisitor extends BaseClassVisitor {
    private ReplaceConfig config;
    private boolean isNeedReplace = true;
    private HashMap<String, GeneratorAdapter> methodNodes = new HashMap<>();
    private boolean isCanGenarate;

    public ReplaceClassVisitor(ClassVisitor cv, UatuContext context) {
        super(Opcodes.ASM5, cv, context);
        this.config = context.getConfig().getReplaceConfig();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        isCanGenarate = !TypeUtil.isInterface(access);
        List<String> needToExclude = config.getNeedToExclude();
        if (needToExclude != null) {
            for (String item : needToExclude) {
                if (name.equals(item)) {
                    isNeedReplace = false;
                    break;
                }
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (!isNeedReplace) {
            return methodVisitor;
        }
        return new ReplaceMethodVisitor(Opcodes.ASM5, methodVisitor, access, name, desc, config, methodNodes, cv,className);
    }

    @Override
    public void visitEnd() {
        if (isCanGenarate) {
            for (Map.Entry<String, GeneratorAdapter> stringGeneratorAdapterEntry : methodNodes.entrySet()) {
                stringGeneratorAdapterEntry.getValue().endMethod();
            }
            /*MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "add", "(II)I", null, null);
            mv.visitCode();
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitInsn(IADD);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(2, 3);
            mv.visitEnd();*/
        }
        super.visitEnd();
    }
}
