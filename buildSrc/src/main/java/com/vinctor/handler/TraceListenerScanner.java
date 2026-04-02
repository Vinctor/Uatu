package com.vinctor.handler;

import com.android.build.api.transform.TransformInvocation;
import com.vinctor.graph.ClassNode;
import com.vinctor.graph.ClassNodeGraph;
import com.vinctor.log.Log;
import com.vinctor.trace.TraceConfig;
import com.vinctor.util.TypeUtil;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 轻量扫描器 - 只查找 ITraceListener 实现类
 * 相比 PreStatisticsHandler，不需要建立完整的类继承图
 */
public class TraceListenerScanner extends BaseHanlder {
    private static final String TAG = "TraceListenerScanner";
    private boolean found = false;

    public TraceListenerScanner(TransformInvocation transformInvocation) {
        super(transformInvocation);
    }

    public static void start(TransformInvocation transformInvocation) {
        new TraceListenerScanner(transformInvocation).start();
    }

    @Override
    byte[] onHanlerFileInput(byte[] bytes) {
        if (!found) {
            scanForTraceListener(bytes);
        }
        return bytes;
    }

    @Override
    byte[] onHanlerJarInput(byte[] bytes) {
        if (!found) {
            scanForTraceListener(bytes);
        }
        return bytes;
    }

    private void scanForTraceListener(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        cr.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                // 跳过抽象类和接口，只找具体实现类
                boolean isAbs = TypeUtil.isAbstract(access);
                boolean isInterface = TypeUtil.isInterface(access);
                if (isAbs || isInterface) {
                    return;
                }
                // 只关注实现了 ITraceListener 的具体类
                if (interfaces != null) {
                    for (String iface : interfaces) {
                        if (TraceConfig.TRACE_CLASS_INTERFACE.equals(iface)) {
                            // 记录这个实现类到类图中
                            ClassNode node = ClassNodeGraph.getClassNode(name);
                            if (node == null) {
                                node = new ClassNode(name);
                            }
                            node.setAbs(false).setInterface(false)
                                .setParent(superName).setInterfaces(interfaces);
                            found = true;
                            Log.w(TAG, "Found ITraceListener implementation: " + name);
                            break;
                        }
                    }
                }
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
}
