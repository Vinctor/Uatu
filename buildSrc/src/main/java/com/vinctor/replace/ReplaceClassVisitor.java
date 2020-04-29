package com.vinctor.replace;

import com.vinctor.BaseClassVisitor;
import com.vinctor.UatuContext;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;

import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;


public class ReplaceClassVisitor extends BaseClassVisitor {
    private ReplaceConfig config;
    private boolean isNeedReplace = true;
    private List<MethodNode> methodNodes = new ArrayList<>();

    public ReplaceClassVisitor(ClassVisitor cv, UatuContext context) {
        super(Opcodes.ASM5, cv, context);
        this.config = context.getConfig().getReplaceConfig();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
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
        return new ReplaceMethodVisitor(Opcodes.ASM5, methodVisitor, access, name, desc, config, methodNodes);
    }

    @Override
    public void visitEnd() {
        /*Method m = Method.getMethod("void main (String[])");
        GeneratorAdapter mg = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, null, null, cv);
        mg.loadArgs();
        mg.returnValue();
        mg.endMethod();*/
        super.visitEnd();
    }
}
