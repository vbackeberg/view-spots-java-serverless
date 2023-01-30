package com.serverless;

import java.util.HashSet;

public class Element {
    private int id;

    private HashSet<Integer> nodes;

    public Element() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashSet<Integer> getNodes() {
        return nodes;
    }

    public void setNodes(HashSet<Integer> nodes) {
        this.nodes = nodes;
    }
}
