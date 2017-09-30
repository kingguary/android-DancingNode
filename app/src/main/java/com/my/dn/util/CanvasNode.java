package com.my.dn.util;

import com.my.dn.data.FloatPoint;
import com.my.dn.data.Node;

/*
 * 代表一个node在canvas中的坐标，以及上下左右四个anchor以供连接线
 */
public class CanvasNode {

    public CanvasNode(Node node) {
        this.node = node;
        this.Center = new FloatPoint();
        this.isDelete = false;
    }

    public int radius;

    public Node node;

    public FloatPoint Center;

    public boolean isDelete;
}
