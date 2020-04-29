package com.vinctor.replace;

import com.vinctor.Log;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.List;

public class ReplaceMethodVisitor extends GeneratorAdapter {

    private final ReplaceConfig config;

    public ReplaceMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc, ReplaceConfig config) {
        super(api, mv, access, name, desc);
        this.config = config;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        List<ReplaceBean> replaceList = config.getReplaceList();
        Log.i("visitMethodInsn", opcode + "-" + name);
        for (ReplaceBean replaceBean : replaceList) {
            ReplaceBean.methodBean from = replaceBean.from;
            ReplaceBean.methodBean to = replaceBean.to;
            if (owner.equals(from.className) && name.equals(from.methodName) && desc.equals(from.methodDesc)) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, to.className, to.methodName, to.methodDesc, itf);
                return;
            }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
