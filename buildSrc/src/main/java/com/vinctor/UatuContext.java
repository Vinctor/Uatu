package com.vinctor;

import com.vinctor.graph.ClassNodeGraph;

public class UatuContext {
    UatuConfig config;
    ClassNodeGraph classNodeGraph = ClassNodeGraph.getSingleton();

    public UatuContext(UatuConfig config) {
        this.config = config;
    }

    public UatuConfig getConfig() {
        return config;
    }

    public ClassNodeGraph getClassNodeGraph() {
        return classNodeGraph;
    }
}
