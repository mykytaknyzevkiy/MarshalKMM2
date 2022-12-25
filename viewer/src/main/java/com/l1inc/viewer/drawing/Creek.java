package com.l1inc.viewer.drawing;

import android.content.Context;
import android.opengl.GLES20;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 3/30/2017.
 */

public class Creek extends BaseDrawingObject {

    private Vector extremeLeft;
    private Vector extremeTop;
    private Vector extremeRight;
    private Vector extremeBottom;

    private int textureResId = -1;
    private Context context;

    private ArrayList<Vector> vertexArrayMain = new ArrayList<>();

    public Vector getExtremeLeft() {
        return extremeLeft;
    }

    public void setExtremeLeft(Vector extremeLeft) {
        this.extremeLeft = extremeLeft;
    }

    public Vector getExtremeTop() {
        return extremeTop;
    }

    public void setExtremeTop(Vector extremeTop) {
        this.extremeTop = extremeTop;
    }

    public Vector getExtremeRight() {
        return extremeRight;
    }

    public void setExtremeRight(Vector extremeRight) {
        this.extremeRight = extremeRight;
    }

    public Vector getExtremeBottom() {
        return extremeBottom;
    }

    public void setExtremeBottom(Vector extremeBottom) {
        this.extremeBottom = extremeBottom;
    }

    public class Cap {

        private FloatBuffer vertexBuffer;
        private FloatBuffer textureBuffer;
        private int vertexCount;

        private ArrayList<Vector> vertexArray = new ArrayList<>();

        public Cap(Vector position, double radius) {

            List<Float> vertexList = new ArrayList<>();
            List<Float> uvList = new ArrayList<>();
            double currentAngle = 0;

            while (currentAngle < Math.PI * 2) {
                float x = (float) (Math.cos(currentAngle) * radius + position.x);
                float y = (float) (Math.sin(currentAngle) * radius + position.y);
                vertexList.add(x);
                vertexList.add(y);
                updateExtremePoints((float) x, (float) y);
                vertexList.add(0f);
                vertexArray.add(new Vector(x, y, 0));
                uvList.add(x);
                uvList.add(y);

                currentAngle += VectorMath.deg2rad(10);
            }

            vertexCount = vertexList.size();
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 4);
            vbb.order(ByteOrder.nativeOrder());
            vertexBuffer = vbb.asFloatBuffer();
            for (Float val : vertexList) {
                vertexBuffer.put(val);
            }
            vertexBuffer.position(0);


            ByteBuffer byteBuf = ByteBuffer.allocateDirect(uvList.size() * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            textureBuffer = byteBuf.asFloatBuffer();
            for (Float val : uvList) {
                textureBuffer.put(val);
            }
            textureBuffer.position(0);
        }

        public void draw(Course3DRenderer renderer) {
            if (Frustum.isPolygonInFrustum(vertexArray))
                drawObjectWithArrays(renderer, GLES20.GL_TRIANGLE_FAN, textureId, vertexBuffer, textureBuffer, vertexCount / 3, 1);
        }
    }

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private int textureId;
    private int vertexCount;
    private Creek.Cap startCap;
    private Creek.Cap endCap;

    private void updateExtremePoints(float lon, float lat) {
        if (extremeLeft == null) {
            extremeLeft = new Vector(lon, lat);
            extremeTop = new Vector(lon, lat);
            extremeRight = new Vector(lon, lat);
            extremeBottom = new Vector(lon, lat);
        } else {
            if (lon < extremeLeft.x) {
                extremeLeft = new Vector(lon, lat);
            }
            if (lon > extremeRight.x) {
                extremeRight = new Vector(lon, lat);
            }
            if (lat < extremeBottom.y) {
                extremeBottom = new Vector(lon, lat);
            }
            if (lat > extremeTop.y) {
                extremeTop = new Vector(lon, lat);
            }
        }
    }

    public Creek(int resId, Context context, PointList pointList, double width) {

        this.textureResId = resId;
        this.context = context;

        List<Float> vertexList = new ArrayList<>();
        List<Float> uvList = new ArrayList<>();

        //String pointLog = "";
        //String lineLog = "";
        Vector[] _pointList = pointList.getPointList();
        for (int i = 0; i < _pointList.length - 1; i++) {

            {
                Vector start = _pointList[i];
                Vector end = _pointList[i + 1];
                Vector diff = end.substracted(start);
                Vector normalized = diff.normalized();
                Vector multiplied = normalized.multiplied(width / 2);
                Vector pt1 = multiplied.rotated(Math.PI / 2);
                pt1 = pt1.added(start);
                Vector pt2 = multiplied.rotated(-Math.PI / 2);
                pt2 = pt2.added(start);

                if (i == 0) {
                    vertexList.add((float) pt1.x);
                    vertexList.add((float) pt1.y);
                    updateExtremePoints((float) pt1.x, (float) pt1.y);
                    vertexList.add(0f);
                    vertexArrayMain.add(new Vector(pt1.x, pt1.y, 0));
                    uvList.add((float) pt1.x);
                    uvList.add((float) pt1.y);
                }

                vertexList.add((float) pt2.x);
                vertexList.add((float) pt2.y);
                updateExtremePoints((float) pt2.x, (float) pt2.y);
                vertexList.add(0f);
                vertexArrayMain.add(new Vector(pt2.x, pt2.y, 0));
                uvList.add((float) pt2.x);
                uvList.add((float) pt2.y);

            }

            {
                Vector start = _pointList[i + 1];
                Vector end = _pointList[i];
                Vector diff = end.substracted(start);
                Vector normalized = diff.normalized();
                Vector multiplied = normalized.multiplied(width / 2);
                Vector pt1 = multiplied.rotated(-Math.PI / 2);
                pt1 = pt1.added(start);
                Vector pt2 = multiplied.rotated(Math.PI / 2);
                pt2 = pt2.added(start);

                vertexList.add((float) pt1.x);
                vertexList.add((float) pt1.y);
                updateExtremePoints((float) pt1.x, (float) pt1.y);
                vertexList.add(0f);
                vertexArrayMain.add(new Vector(pt1.x, pt1.y, 0));
                uvList.add((float) pt1.x);
                uvList.add((float) pt1.y);

                if (i == _pointList.length - 2) {

                    vertexList.add((float) pt2.x);
                    vertexList.add((float) pt2.y);
                    updateExtremePoints((float) pt2.x, (float) pt2.y);
                    vertexList.add(0f);
                    vertexArrayMain.add(new Vector(pt2.x, pt2.y, 0));
                    uvList.add((float) pt2.x);
                    uvList.add((float) pt2.y);
                }
            }
        }


        startCap = new Creek.Cap(_pointList[0], width / 2);
        endCap = new Creek.Cap(_pointList[_pointList.length - 1], width / 2);


        vertexCount = vertexList.size();
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        for (Float val : vertexList) {
            vertexBuffer.put(val);
        }
        vertexBuffer.position(0);


        ByteBuffer byteBuf = ByteBuffer.allocateDirect(uvList.size() * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        for (Float val : uvList) {
            textureBuffer.put(val);
        }
        textureBuffer.position(0);


        //loadTexture();
/*
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();*/

    }

    private void loadTexture() {
        textureId = TextureCache.getTexture(context, textureResId, true, GLES20.GL_REPEAT);
    }

    public void draw(Course3DRenderer renderer) {

        if (textureId == -1 || !TextureCache.hasTexture(textureResId,textureId)) {
            loadTexture();
        }

        if (Frustum.isPolygonInFrustum(vertexArrayMain))
            drawObjectWithArrays(renderer, GLES20.GL_TRIANGLE_STRIP, textureId, vertexBuffer, textureBuffer, vertexCount / 3, 1);
        startCap.draw(renderer);
        endCap.draw(renderer);
    }

    public void destroy() {
        //GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
    }
}
