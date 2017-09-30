package com.my.dn.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import com.my.dn.App;
import com.my.dn.R;
import com.my.dn.data.Edge;
import com.my.dn.data.FloatPoint;

public class CanvasDrawer {

    private static final String TAG = "CanvasDrawer";

    private static CanvasDrawer mInstance;

    private CanvasData mCanvasData;
    private Canvas mCanvas;
    private Paint mPaint;
    private Paint mPenPaint;

    public static CanvasDrawer geInstance() {
        if (null == mInstance) {
            mInstance = new CanvasDrawer();
        }
        return mInstance;
    }

    private CanvasDrawer() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(App.getInstance().getResources().getColor(R.color.C1));
        mPenPaint = new Paint();
        mPenPaint.setAntiAlias(true);
        mPenPaint.setStyle(Paint.Style.FILL);
        mPenPaint.setTextSize(30.0f);
        mPenPaint.setColor(App.getInstance().getResources().getColor(R.color.B1));
        mPenPaint.setTextAlign(Align.CENTER);
    }

    public void setDrawContent(CanvasData data, Canvas canvas) {
        mCanvasData = data;
        mCanvas = canvas;
    }

    public void draw() {
        Log.d(TAG, "draw");
        if (null == mCanvasData) {
            return;
        }

        List<Edge> edges = mCanvasData.getEdges();
        if (null == edges || edges.size() == 0) {
            return;
        }

        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            CanvasNode sNode = mCanvasData.getCanvasNode(edge.source);
            CanvasNode eNode = mCanvasData.getCanvasNode(edge.target);
            if (null == sNode || null == eNode ||
                    sNode.isDelete || eNode.isDelete) {
                continue;
            }

            if (!IsCirclesIntersect(sNode.Center, sNode.radius, eNode.Center, eNode.radius)) {
                List<Point> sourceIntersectionPoints = getCircleLineIntersectionPoint(sNode.Center, eNode.Center,
                        sNode.Center, sNode.radius);
                // Log.d(TAG, "sourceIntersectionPoints:" + sourceIntersectionPoints);
                List<Point> targetIntersectionPoints = getCircleLineIntersectionPoint(sNode.Center, eNode.Center,
                        eNode.Center, eNode.radius);
                // Log.d(TAG, "targetIntersectionPoints:" + targetIntersectionPoints);
                if ((null != sourceIntersectionPoints && sourceIntersectionPoints.size() > 0)
                        && (null != targetIntersectionPoints && targetIntersectionPoints.size() > 0)) {
                    Point sourcePoint = sourceIntersectionPoints.get(1);
                    Point targetPoint = targetIntersectionPoints.get(0);
                    drawArrowLine(sourcePoint, targetPoint);
                    drawTextOnLine(sourcePoint, targetPoint, edge);

                    // just for test
                    // mCanvas.drawCircle(sNode.Center.x, sNode.Center.y, sNode.radius, paint);
                    // mCanvas.drawCircle(eNode.Center.x, eNode.Center.y, eNode.radius, paint);
                }
            } else {
                Log.d(TAG, "node:" + sNode.node.name + " intersetcs node:" + eNode.node.name);
            }
        }
    }

    private void drawArrowLine(Point source, Point target) {
        drawArrowLine(source.x, source.y, target.x, target.y);
    }

    /**
     * 画带箭头的线
     */
    private void drawArrowLine(float startX, float startY, float endX, float endY) {
        double H = 8; // 箭头高度
        double L = 3.5; // 底边的一半
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H); // 箭头角度
        double arraow_len = Math.sqrt(L * L + H * H); // 箭头的长度
        double[] arrXY_1 = rotateVec(endX - startX, endY - startY, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(endX - startX, endY - startY, -awrad, true, arraow_len);
        double x_3 = endX - arrXY_1[0]; // (x3,y3)是第一端点
        double y_3 = endY - arrXY_1[1];
        double x_4 = endX - arrXY_2[0]; // (x4,y4)是第二端点
        double y_4 = endY - arrXY_2[1];
        Double X3 = Double.valueOf(x_3);
        x3 = X3.intValue();
        Double Y3 = Double.valueOf(y_3);
        y3 = Y3.intValue();
        Double X4 = Double.valueOf(x_4);
        x4 = X4.intValue();
        Double Y4 = Double.valueOf(y_4);
        y4 = Y4.intValue();
        // 画线
        mCanvas.drawLine(startX, startY, endX, endY, mPaint);
        Path triangle = new Path();
        triangle.moveTo(endX, endY);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.close();
        mCanvas.drawPath(triangle, mPaint);
    }

    // 计算
    private double[] rotateVec(float px, float py, double ang, boolean isChLen, double newLen) {
        double mathstr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }

    private void drawTextOnLine(Point sourcePoint, Point targetPoint, Edge edge) {
        List<String> labels = edge.labels;
        if (null != labels && labels.size() > 0) {
            StringBuilder strBuilder = new StringBuilder();
            for (int j = 0; j < labels.size(); j++) {
                if (j != 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(labels.get(j));
            }

            Path path = new Path();
            path.moveTo(sourcePoint.x, sourcePoint.y);
            path.lineTo(targetPoint.x, targetPoint.y);
            mCanvas.drawTextOnPath(strBuilder.toString(), path, 0, 0, mPenPaint);
        }
    }

    /*
     * 获取一条直线与圆的相交点
     */
    private List<Point> getCircleLineIntersectionPoint(FloatPoint source, FloatPoint target, FloatPoint center, int radius) {
        double baX = target.x - source.x;
        double baY = target.y - source.y;
        double caX = center.x - source.x;
        double caY = center.y - source.y;

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        Point p1 = new Point((int) (source.x - baX * abScalingFactor1), (int) (source.y - baY * abScalingFactor1));
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }
        Point p2 = new Point((int) (source.x - baX * abScalingFactor2), (int) (source.y - baY * abScalingFactor2));
        return Arrays.asList(p1, p2);
    }

    /*
     * / 判断两个圆是否相交
     */
    private boolean IsCirclesIntersect(FloatPoint centerA, int radiusA, FloatPoint centerB, int radiusB) {
        // 两个圆心之间的距离的平方
        return Math.sqrt((centerA.x - centerB.x) * (centerA.x - centerB.x)
                + (centerA.y - centerB.y) * (centerA.y - centerB.y)) < (radiusA + radiusB);
    }
}
