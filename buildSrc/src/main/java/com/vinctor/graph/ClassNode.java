package com.vinctor.graph;

import com.vinctor.util.NodeUtil;

import org.apache.http.util.TextUtils;

import java.util.HashSet;
import java.util.Set;

public class ClassNode {
    public final static int CLASS = 1;
    public final static int ABSTRACT_CLASS = 2;
    public final static int INTERFACE = 3;

    int type = -1;
    public String name;
    ClassNode parent;
    HashSet<ClassNode> interfaces = new HashSet<>();
    HashSet<ClassNode> children = new HashSet<>();

    public ClassNode(String name) {
        this.type = type;
        this.name = parseClassName(name);
        addToGraph(this);
    }

    public String getName() {
        return name;
    }

    public HashSet<ClassNode> getChildren() {
        return children;
    }

    public ClassNode getParent() {
        return parent;
    }

    public HashSet<ClassNode> getInterfaces() {
        return interfaces;
    }

    public ClassNode setParent(String className) {
        if (TextUtils.isEmpty(className) || "java/lang/Object".equals(className))
            return this;
        className = parseClassName(className);
        parent = ClassNodeGraph.getClassNode(className);
        if (parent == null) {
            parent = new ClassNode(className);
        }
        parent.addChild(this);
        return this;
    }

    public ClassNode setInterfaces(String[] interfaces) {
        if (interfaces == null || interfaces.length == 0)
            return this;
        for (String anInterface : interfaces) {
            anInterface = parseClassName(anInterface);
            addInterfaceClass(anInterface);
        }

        return this;
    }

    public ClassNode addInterfaceClass(String interfaceName) {
        if (TextUtils.isEmpty(interfaceName)) return this;
        ClassNode interfaceClass = ClassNodeGraph.getClassNode(interfaceName);
        if (interfaceClass == null) {
            interfaceClass = new ClassNode(interfaceName);
        }
        interfaces.add(interfaceClass);
        interfaceClass.addChild(this);
        return this;
    }


    private void addChild(ClassNode classNode) {
        if (classNode == null) return;
        for (ClassNode child : children) {
            if (child.getName().equals(classNode.getName())) {
                return;
            }
        }
        children.add(classNode);
    }

    void addToGraph(ClassNode classNode) {
        ClassNodeGraph.getSingleton().addNode(classNode);
    }

    public static String parseClassName(String className) {
        return className.replace("/", ".");
    }

    public Set<ClassNode> findAllParentAndInterfaces() {
        Set<ClassNode> set = new HashSet<>();
        NodeUtil.findAllParentAndInterfaces(this, set);
        return set;
    }

    public Set<ClassNode> getAllChildren() {
        Set<ClassNode> set = new HashSet<>();
        NodeUtil.getAllChildren(this, set);
        return set;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (name == null) return false;
        if (!(obj instanceof ClassNode)) return false;
        return name.equals((((ClassNode) obj).name));
    }

    @Override
    public String toString() {
        return "ClassNode{" +
                ", name='" + name + '\'' +
                ", parent=" + (parent == null ? "" : parent.name) +
                ", interfaces=" + interfaces.size() +
                ", children=" + children.size() +
                '}';
    }
}
