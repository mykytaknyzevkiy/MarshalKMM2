package com.l1inc.viewer;

import android.view.MotionEvent;

/**
 * Created by Yevhen Paschenko on 2/24/2016.
 */
public class PanGestureDetector {
    private static final int INVALID_POINTER_ID = -1;
    private float fX, fY;
    private int ptrID;
    private float mAngle;
    private float deltaX;
    private float deltaY;

    private OnPanGestureListener mListener;

    public float getDeltaX() {
        return deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }

    public PanGestureDetector(OnPanGestureListener listener) {
        mListener = listener;
        ptrID = INVALID_POINTER_ID;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean retval = false;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                ptrID = event.getPointerId(event.getActionIndex());
                fX = event.getX();
                fY = event.getY();
                if (mListener != null) {
                    mListener.OnPanBegin(this, event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (ptrID != INVALID_POINTER_ID) {
                    float nfX, nfY;
                    nfX = event.getX();
                    nfY = event.getY();
                    deltaX = fX - nfX;
                    deltaY = fY - nfY;

                    if ((Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) && mListener != null) {
                        mListener.OnPan(this, event);
                    }

                }

                retval = true;
                break;
            case MotionEvent.ACTION_UP:
                ptrID = INVALID_POINTER_ID;
                if (mListener != null) {
                    mListener.OnPanEnd(this, event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                ptrID = INVALID_POINTER_ID;
                if (mListener != null) {
                    mListener.OnPanEnd(this, event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                ptrID = INVALID_POINTER_ID;
                if (mListener != null) {
                    mListener.OnPanEnd(this, event);
                }
                break;
        }
        return retval;
    }

    public void simulateTouchDown(MotionEvent event){
        ptrID = event.getPointerId(event.getActionIndex());
        fX = event.getX();
        fY = event.getY();
        if (mListener != null) {
            mListener.OnPanBegin(this, event);
        }
    }

    public static interface OnPanGestureListener {
        public void OnPanBegin(PanGestureDetector panGestureDetector, MotionEvent event);

        public void OnPan(PanGestureDetector panGestureDetector, MotionEvent event);

        public void OnPanEnd(PanGestureDetector panGestureDetector, MotionEvent event);
    }
}
