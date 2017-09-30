package com.my.dn.multitouch;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.my.dn.R;
import com.my.dn.data.FloatPoint;
import com.my.dn.widget.NodeView;

/**
 * A long press event detector.
 */
public class CanvasNodeLongTouchListener implements View.OnTouchListener {
    private static final String TAG = "SkyEyePanel";

    // Default long press time threshold.
    private static final long LONG_PRESS_TIME_THRESHOLD = 300;
    // Long press event message handler.
    private Handler mHandler = new Handler();
    // The long press time threshold.
    private long mPressTimeThreshold;
    // Record start point and end point to judge whether user has moved while
    // performing long press event.
    private FloatPoint mTouchStartPoint = new FloatPoint();
    private FloatPoint mTouchEndPoint = new FloatPoint();
    private FloatPoint mTouchPrevPoint = new FloatPoint();
    private FloatPoint mTouchCurPoint = new FloatPoint();
    // The long press thread.
    private final LongPressThread mLongPressThread = new LongPressThread();
    // Inset in pixels to look for touchable content when the user touches the
    // edge of the screen.
    private final float mTouchSlop;

    private NodeView mCanvasView;
    private OnViewTouchedListener mListener;

    public CanvasNodeLongTouchListener(NodeView view, OnViewTouchedListener listener) {
        this(view, LONG_PRESS_TIME_THRESHOLD, listener);
    }

    public CanvasNodeLongTouchListener(NodeView view, long holdTime, OnViewTouchedListener listener) {
        mCanvasView = view;
        mCanvasView.setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(view.getContext()).getScaledEdgeSlop();
        Log.d(TAG, "touch slop:" + mTouchSlop);
        mPressTimeThreshold = holdTime;
        this.mListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartPoint.x = event.getRawX();
                mTouchStartPoint.y = event.getRawY();
                mTouchPrevPoint.x = event.getRawX();
                mTouchPrevPoint.y = event.getRawY();
                onTouchBegin();
                return true;
            case MotionEvent.ACTION_MOVE:
                mTouchEndPoint.x = event.getRawX();
                mTouchEndPoint.y = event.getRawY();
                mTouchCurPoint.x = event.getRawX();
                mTouchCurPoint.y = event.getRawY();
                float deltaCenterX = mTouchCurPoint.x - mTouchPrevPoint.x;
                float deltaCenterY = mTouchCurPoint.y - mTouchPrevPoint.y;
                mTouchPrevPoint.x = mTouchCurPoint.x;
                mTouchPrevPoint.y = mTouchCurPoint.y;
                if (mListener != null) {
                    mListener.onViewMoved((NodeView) v, deltaCenterX, deltaCenterY);
                }
                return true;
            case MotionEvent.ACTION_UP:
                onTouchEnd();
                int curX = (int) event.getRawX();
                int curY = (int) event.getRawY();
                if (Math.abs(curX - mTouchStartPoint.x) < 5 && Math.abs(curY - mTouchStartPoint.y) < 5) {
                    if (mListener != null) {
                        mListener.onViewClicked(v);
                    }
                }
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Reset the long press event.
     */
    private void resetLongPressEvent() {
        if (mLongPressThread.mAdded) {
            mHandler.removeCallbacks(mLongPressThread);
            mLongPressThread.mAdded = false;
        }
        mLongPressThread.mLongPressing = false;
    }

    /**
     * Add long press event handler.
     */
    private void addLongPressCallback() {
        if (!mLongPressThread.mAdded) {
            mLongPressThread.mLongPressing = false;
            mHandler.postDelayed(mLongPressThread, mPressTimeThreshold);
            mLongPressThread.mAdded = true;
        }
    }

    /**
     * Calculate distance between two point.
     *
     * @param before previous point
     * @param after  next point
     * @return the distance
     */
    private double calculateDistanceBetween(FloatPoint before, FloatPoint after) {
        return Math.sqrt(Math.pow((before.x - after.x), 2) + Math.pow((before.y - after.y), 2));
    }

    /**
     * Judge whether the long press event happens.
     * <p>
     * The time threshold of default activated event is
     * {@see #LONG_PRESS_TIME_THRESHOLD}
     */
    private class LongPressThread implements Runnable {
        // A flag to set whether the long press event happens.
        boolean mLongPressing = false;
        // A flag to set whether this thread has been added to the handler.
        boolean mAdded = false;

        @Override
        public void run() {
            mLongPressing = true;
            // onLongTouchBegin();
        }
    }

    private void onTouchBegin() {
        if (null == mCanvasView.getNode()) {
            return;
        }

        mCanvasView.setBackgroundResource(R.mipmap.humen_drag);
    }

    private void onTouchEnd() {
        if (null == mCanvasView.getNode()) {
            return;
        }
        mCanvasView.setBackgroundResource(R.mipmap.local_node);
    }

    public interface OnViewTouchedListener {
        public void onViewMoved(NodeView view, float deltaX, float deltaY);

        public void onViewClicked(View v);
    }
}