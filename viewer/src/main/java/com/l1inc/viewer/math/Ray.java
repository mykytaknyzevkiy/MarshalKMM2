package com.l1inc.viewer.math;

import android.opengl.GLES20;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Yevhen Paschenko on 6/6/2016.
 */
public class Ray {

    public float[] P0;
    public float[] P1;

    public Ray(final int width,
               final int height,
               final float xTouch,
               final float yTouch,
               final float[] modelViewMatrix,
               final float[] projectionMatrix) {

        int[] viewport = {0, 0, width, height};

        float[] nearCoOrds = new float[4];
        float[] farCoOrds = new float[4];
        float[] temp = new float[4];
        float[] temp2 = new float[4];
        // get the near and far ords for the click

        float winx = xTouch, winy = (float) viewport[3] - yTouch;

        int result = GLU.gluUnProject(winx, winy, 1.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, temp, 0);
        Matrix.multiplyMV(temp2, 0, modelViewMatrix, 0, temp, 0);

        if (result == GLES20.GL_TRUE) {
            nearCoOrds[0] = temp2[0] / temp2[3];
            nearCoOrds[1] = temp2[1] / temp2[3];
            nearCoOrds[2] = temp2[2] / temp2[3];
        }

        result = GLU.gluUnProject(winx, winy, 0.0f, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, temp, 0);
        Matrix.multiplyMV(temp2, 0, modelViewMatrix, 0, temp, 0);

        if (result == GLES20.GL_TRUE) {
            farCoOrds[0] = temp2[0] / temp2[3];
            farCoOrds[1] = temp2[1] / temp2[3];
            farCoOrds[2] = temp2[2] / temp2[3];

        }
        this.P0 = farCoOrds;
        this.P1 = nearCoOrds;
    }

}
