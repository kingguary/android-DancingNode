package com.my.dn.util;

import com.my.dn.data.Edge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 管理所有要绘画的Canvas Node数据，作为一个单例存在
 */
public class CanvasData {

    private Map<String, CanvasNode> mNodesMap;
    private List<Edge> mEdges;

    public CanvasData(List<Edge> edges) {
        mNodesMap = new HashMap<String, CanvasNode>();
        mEdges = edges;
    }

    public Map<String, CanvasNode> getNodesMap() {
        return mNodesMap;
    }

    public List<Edge> getEdges() {
        return mEdges;
    }

    public void clearAllNodes() {
        mNodesMap.clear();
    }

    public void updateNode(CanvasNode node) {
        mNodesMap.put(node.node.id, node);
    }

    public void deleteNode(String nodeId) {
        if (mNodesMap.containsKey(nodeId)) {
            mNodesMap.remove(nodeId);
        }
    }

    public void addNode(CanvasNode node) {
        mNodesMap.put(node.node.id, node);
    }

    public CanvasNode getCanvasNode(String nodeId) {
        return mNodesMap.get(nodeId);
    }

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("It has ").append(mNodesMap.size()).append(" nodes").append(" and it has ")
                .append(null == mEdges ? 0 : mEdges.size()).append(" edges");
        return strBuilder.toString();
    }
}
