package com.l1inc.viewer.drawing;

import android.content.Context;
import android.opengl.ETC1Util;
import android.opengl.GLES20;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Interpolator;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;
import com.l1inc.viewer.parcer.Shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 5/25/2016.
 */
public class OutlinePolygon extends BaseDrawingObject {


    private Vector extremeLeft;
    private Vector extremeTop;
    private Vector extremeRight;
    private Vector extremeBottom;

    private int textureResId = -1;
    private Context context;
    private boolean isTextureCompressed = false;

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
                vertexList.add(0f);
                vertexArray.add(new Vector(x, y, 0));
                uvList.add(x);
                uvList.add(y);
                //updateExtremePoints(x, y);
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

    private int textureId = -1;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private int vertexCount;
    private Cap endCap;


    public OutlinePolygon(final Shape shape,
                          final Context context,
                          final int textureResId,
                          final float width,
                          final Layer.PointInterpolation interpolation, boolean isTextureCompressed) throws Exception {

        this.isTextureCompressed = isTextureCompressed;
        this.context = context;
        this.textureResId = textureResId;

        final String points = shape.getPoints();
        final String[] lonLatPointsArray = points.split(",");
        List<Vector> rawPointList = new ArrayList<>();
        for (int i = 0; i < lonLatPointsArray.length; i++) {
            final String lonLatPoints = lonLatPointsArray[i];
            final String[] lonLatPair = lonLatPoints.split(" ");
            final float lat = (float) Layer.transformLat(lonLatPair[1]);
            final float lon = (float) Layer.transformLon(lonLatPair[0]);
            final Vector newVector = new Vector(lon, lat);
            if (rawPointList.size() > 0) {
                final Vector prev = rawPointList.get(rawPointList.size() - 1);
                if (prev.equals(newVector)) {
                    continue;
                }
            }
            rawPointList.add(newVector);
        }
        if (!rawPointList.get(rawPointList.size() - 1).equals(rawPointList.get(0))) {
            rawPointList.add(rawPointList.get(0));
        }

        VectorMath.VectorOrder vectorOrder = VectorMath.getVectorOrder(rawPointList);
        if (vectorOrder == VectorMath.VectorOrder.CCW) {
            Collections.reverse(rawPointList);
        }

        if (interpolation == Layer.PointInterpolation.Interpolate) {
            rawPointList = new ArrayList<>(Interpolator.interpolate(rawPointList, 3, Interpolator.CatmullRomType.Centripetal));
        }

        /*final float[] vertices = new float[rawPointList.size() * 3];
        for (int i = 0; i < rawPointList.size(); i++) {
            final float lon = (float) rawPointList.get(i).x;
            final float lat = (float) rawPointList.get(i).y;
            vertices[i * 3 + 0] = lon;
            vertices[i * 3 + 1] = lat;
            vertices[i * 3 + 2] = 0;
        }*/

        List<Float> vertexList = new ArrayList<>();
        List<Float> uvList = new ArrayList<>();

        Float baseU = null;
        Float baseV = null;

        final Vector[] _pointList = rawPointList.toArray(new Vector[rawPointList.size()]);
        for (int i = 0; i < _pointList.length - 1; i++) {

            if (baseU == null || baseV == null) {
                baseU = (float) _pointList[i].x;
                baseV = (float) _pointList[i].y;
            }

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
                    vertexList.add(0f);
                    updateExtremePoints((float) pt1.x - 0, (float) pt1.y - 0);
                    uvList.add((float) pt1.x - baseU);
                    uvList.add((float) pt1.y - baseV);
                }
                vertexList.add((float) pt2.x);
                vertexList.add((float) pt2.y);
                vertexList.add(0f);
                updateExtremePoints((float) pt2.x - 0, (float) pt2.y - 0);
                uvList.add((float) pt2.x - baseU);
                uvList.add((float) pt2.y - baseV);
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
                vertexList.add(0f);
                updateExtremePoints((float) pt1.x - 0, (float) pt1.y - 0);
                uvList.add((float) pt1.x - baseU);
                uvList.add((float) pt1.y - baseV);

                if (i == _pointList.length - 2) {
                    vertexList.add((float) pt2.x);
                    vertexList.add((float) pt2.y);
                    vertexList.add(0f);
                    updateExtremePoints((float) pt2.x - 0, (float) pt2.y - 0);
                    uvList.add((float) pt2.x - baseU);
                    uvList.add((float) pt2.y - baseV);
                }
            }
        }

        endCap = new Cap(_pointList[_pointList.length - 1], width / 2);

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

    }

    public void draw(Course3DRenderer renderer) {
        if (textureId == -1 || !TextureCache.hasTexture(textureResId, textureId)) {
            loadTexture();
        }

        drawObjectWithArrays(renderer, GLES20.GL_TRIANGLE_STRIP, textureId, vertexBuffer, textureBuffer, vertexCount / 3, 1);
        endCap.draw(renderer);
    }

    private void loadTexture() {
        if (!isTextureCompressed)
            textureId = TextureCache.getTexture(context, textureResId);
        else
            textureId = !ETC1Util.isETC1Supported() ? TextureCache.getTexture(context, textureResId) : TextureCache.getCompressedTexture(context, textureResId);
    }

    public void destroy() {
        // GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
    }
}
