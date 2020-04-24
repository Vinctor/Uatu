package com.vinctor.classtree;

import org.apache.http.util.TextUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClassNodeGraph {
    private volatile static ClassNodeGraph singleton;

    private ClassNodeGraph() {
    }

    public static ClassNodeGraph getSingleton() {
        if (singleton == null) {
            synchronized (ClassNodeGraph.class) {
                if (singleton == null) {
                    singleton = new ClassNodeGraph();
                }
            }
        }
        return singleton;
    }

    private Set<ClassNode> set = Collections.synchronizedSet(new HashSet<>());

    public static synchronized ClassNode getClassNode(String className) {
        if (TextUtils.isEmpty(className)) return null;
        ClassNodeGraph singleton = getSingleton();
        Iterator<ClassNode> iterator = singleton.set.iterator();
        while (iterator.hasNext()) {
            ClassNode next = iterator.next();
            if (className.equals(next.getName())) {
                return next;
            }
        }
        return null;
    }

    static synchronized void addNode(ClassNode classNode) {
        getSingleton().set.add(classNode);
    }
}
