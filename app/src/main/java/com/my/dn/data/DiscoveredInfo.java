package com.my.dn.data;

import java.util.List;

public class DiscoveredInfo {

    private List<Edge> edges;
    private List<Node> localNodes;
    private List<Node> discoveredNodes;

    public DiscoveredInfo() {
        localNodes = null;
        discoveredNodes = null;
        edges = null;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public List<Edge> getEdges() {
        return this.edges;
    }

    public void setLocalNodes(List<Node> nodeList) {
        this.localNodes = nodeList;
    }

    public List<Node> getLocalNodes() {
        return localNodes;
    }

    public void setDiscoveredNodes(List<Node> nodeList) {
        this.discoveredNodes = nodeList;
    }

    public List<Node> getDiscoveredNodes() {
        return discoveredNodes;
    }

}
