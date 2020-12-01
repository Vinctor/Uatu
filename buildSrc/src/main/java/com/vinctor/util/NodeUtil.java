package com.vinctor.util;

import com.vinctor.graph.ClassNode;
import com.vinctor.log.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    public static void getAllLeafChilren(ClassNode root, Set<ClassNode> result) {
        if (result == null) {
            return;
        }
        ClassNode classNode = root;
        Set<ClassNode> children = classNode.getChildren();
        for (ClassNode child : children) {
            if (child == null) {
                continue;
            }
            if (child.getChildren().size() == 0) {
                result.add(child);
                continue;
            }
            getAllLeafChilren(child, result);
        }
    }
}
