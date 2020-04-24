package com.vinctor.util;

import com.vinctor.classtree.ClassNode;

import java.util.HashSet;
import java.util.Set;

public class NodeUtil {
    public static void findAllParentAndInterfaces(ClassNode node, Set<ClassNode> set) {
        if (set == null) return;
        ClassNode parent = node.getParent();
        if (parent != null) {
            set.add(parent);
            findAllParentAndInterfaces(parent, set);
        }
        for (ClassNode anInterface : node.getInterfaces()) {
            if (anInterface == null) {
                continue;
            }
            set.add(anInterface);
            findAllParentAndInterfaces(anInterface, set);
        }
    }

    public static void getAllChildren(ClassNode classNode, Set<ClassNode> set) {
        if (set == null) {
            return;
        }
        HashSet<ClassNode> children = classNode.getChildren();
        for (ClassNode child : children) {
            if (child == null) {
                continue;
            }
            set.add(child);
            getAllChildren(child, set);
        }
    }
}
