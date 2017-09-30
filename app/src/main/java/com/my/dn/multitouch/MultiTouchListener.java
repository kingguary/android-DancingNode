package com.my.dn.multitouch;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MultiTouchListener implements OnTouchListener {

    private static final String TAG = "MultiTouch";

    private static final int INVALID_POINTER_ID = -1;
    public boolean isRotateEnabled = false;
    public boolean isTranslateEnabled = true;
    public boolean isScaleEnabled = true;
    public float minimumScale = 1.0f;
    public float maximumScale = 2.0f;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float mPrevX;
    private float mPrevY;
    private float currentScale = minimumScale;
    private float transXDeltaMax = 0.0f;
    private float transYDeltaMax = 0.0f;
    private ScaleGestureDetector mScaleGestureDetector;

    public MultiTouchListener() {
        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener());
    }

    private static float adjustAngle(float degrees) {
        if (degrees > 180.0f) {
            degrees -= 360.0f;
        } else if (degrees < -180.0f) {
            degrees += 360.0f;
        }

        return degrees;
    }

    private void move(View view, TransformInfo info) {
        // Log.d(TAG, "move:" + info.toString());
        computeRenderOffset(view, info.pivotX, info.pivotY);
        adjustTranslation(view, info.deltaX, info.deltaY);

        // Assume that scaling still maintains aspect ratio.
        float scale = view.getScaleX() * info.deltaScale;
        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale));
        currentScale = scale;
        // Log.d(TAG, "currentScale=" + currentScale);
        if (currentScale == minimumScale) {
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
        } else {
            float width = view.getWidth();
            float scaledWidth = width * currentScale;
            float height = view.getHeight();
            float scaledHeight = height * currentScale;
            transXDeltaMax = (scaledWidth - width) / 2;
            transYDeltaMax = (scaledHeight - height) / 2;
        }

        view.setScaleX(scale);
        view.setScaleY(scale);

        float rotation = adjustAngle(view.getRotation() + info.deltaAngle);
        view.setRotation(rotation);
    }

    private void adjustTranslation(View view, float deltaX, float deltaY) {
        // Log.d(TAG, "adjustTranslation deltaX=" + deltaX + " deltaY=" +
        // deltaY);
        if (currentScale == minimumScale) {
            Log.d(TAG, "current scale is minimum, dont allow to transpaltion");
            return;
        }

        float[] deltaVector = {deltaX, deltaY};
        view.getMatrix().mapVectors(deltaVector);
        // Log.d(TAG, "deltaVector[0]=" + deltaVector[0]);
        // Log.d(TAG, "deltaVector[1]=" + deltaVector[1]);
        // Log.d(TAG, "View width=" + view.getWidth());

        float computedTransX = view.getTranslationX() + deltaVector[0];
        float computedTransY = view.getTranslationY() + deltaVector[1];
        // Log.d(TAG, "transXDeltaMax=" + transXDeltaMax + " transYDeltaMax=" +
        // transYDeltaMax);
        // Log.d(TAG, "computedTransX=" + computedTransX + " computedTransY=" +
        // computedTransY);
        if (Math.abs(computedTransX) > transXDeltaMax || (Math.abs(computedTransY) > transYDeltaMax)) {
            // Log.d(TAG, "can not translation anymore");
            return;
        }

        view.setTranslationX(computedTransX);
        view.setTranslationY(computedTransY);
    }

    private static void computeRenderOffset(View view, float pivotX, float pivotY) {
        if (view.getPivotX() == pivotX && view.getPivotY() == pivotY) {
            return;
        }

        float[] prevPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(prevPoint);

        view.setPivotX(pivotX);
        view.setPivotY(pivotY);

        float[] currPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(currPoint);

        float offsetX = currPoint[0] - prevPoint[0];
        float offsetY = currPoint[1] - prevPoint[1];

        view.setTranslationX(view.getTranslationX() - offsetX);
        view.setTranslationY(view.getTranslationY() - offsetY);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(view, event);

        if (!isTranslateEnabled) {
            return true;
        }

        int action = event.getAction();
        switch (action & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mPrevX = event.getX();
                mPrevY = event.getY();

                // Save the ID of this pointer.
                mActivePointerId = event.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // Find the index of the active pointer and fetch its position.
                int pointerIndex = event.findPointerIndex(mActivePointerId);
                if (pointerIndex != -1) {
                    float currX = event.getX(pointerIndex);
                    float currY = event.getY(pointerIndex);

                    // Only move if the ScaleGestureDetector isn't processing a
                    // gesture.
                    if (!mScaleGestureDetector.isInProgress()) {
                        adjustTranslation(view, currX - mPrevX, currY - mPrevY);
                    }
                }

                break;
            }

            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                // Extract the index of the pointer that left the touch sensor.
                int pointerIndex = (action
                        & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mPrevX = event.getX(newPointerIndex);
                    mPrevY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }

                break;
            }
        }

        return true;
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mPivotX;
        private float mPivotY;
        private Vector2D mPrevSpanVector = new Vector2D();

        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());
            return true;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            TransformInfo info = new TransformInfo();
            info.deltaScale = isScaleEnabled ? detector.getScaleFactor() : 1.0f;
            info.deltaAngle = isRotateEnabled ? Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector())
                    : 0.0f;
            info.deltaX = isTranslateEnabled ? detector.getFocusX() - mPivotX : 0.0f;
            info.deltaY = isTranslateEnabled ? detector.getFocusY() - mPivotY : 0.0f;
            info.pivotX = mPivotX;
            info.pivotY = mPivotY;
            info.minimumScale = minimumScale;
            info.maximumScale = maximumScale;

            move(view, info);
            return false;
        }
    }

    private class TransformInfo {
        public float deltaX;
        public float deltaY;
        public float deltaScale;
        public float deltaAngle;
        public float pivotX;
        public float pivotY;
        public float minimumScale;
        public float maximumScale;

        @Override
        public String toString() {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("deltaX=" + deltaX + " deltaY=" + deltaY + " deltaScale=" + deltaScale + " deltaAngle="
                    + deltaAngle + " pivotX=" + pivotX + " pivotY=" + pivotY + " minimumScale=" + minimumScale
                    + " maximumScale" + maximumScale);
            return strBuilder.toString();
        }
    }
}