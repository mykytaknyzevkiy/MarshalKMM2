package com.l1inc.viewer.drawing;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.Course3DRendererBase;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.LinearInterpolator;
import com.l1inc.viewer.math.Vector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Yevhen Paschenko on 3/29/2016.
 */
public class Tree {

    public static Comparator comparator = new Comparator<Tree>() {
        @Override
        public int compare(final Tree lhs, final Tree rhs) {
            return Float.compare(rhs.getyPosition(), lhs.getyPosition());
        }
    };


    private final float TREE_IGOLF_SIZE = 0.35f;
    private final float TREE_YAMAHA_SIZE = 0.45f;

    private final float TreeSize = TREE_YAMAHA_SIZE;

    private class MatrixCalculator {
        final float[] modelViewMatrix = new float[16];
        final float[] modelViewProjectionMatrix = new float[16];
        final float[] vIn = new float[]{0, 0, 0, 1};
        final float[] vOut = new float[4];

        public void calculateMatrices(final float x,
                                      final float y,
                                      final float z,
                                      final float viewAngle,
                                      final float rotationAngle,
                                      final float[] projectionMatrix) {
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, 0);
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
            Matrix.multiplyMV(vOut, 0, modelViewProjectionMatrix, 0, vIn, 0);
            yPosition = vOut[1];
            zPosition = vOut[2] / vOut[3];
        }
    }

    private static final float SHADOW_OPACITY = 35 / 100.0f;

    private static Map<Integer, IndexedTexturedSquare> polygonMap = new HashMap<>();
    private static Random random = new Random(System.currentTimeMillis());

    public static void invalidate() {
        for (Integer key : polygonMap.keySet()) {
            polygonMap.get(key).destroy();
        }
        polygonMap.clear();
    }

    private Vector position;
    private IndexedTexturedSquare tree3DPolygon;
    private IndexedTexturedSquare tree2DPolygon;
    private IndexedTexturedSquare shadowPolygon;
    private float zPosition;
    private float yPosition;
    private int additionalRotation;
    private long lastTimeMillis;
    private LinearInterpolator yrotator;
    private boolean enableAnimation = false;
    private MatrixCalculator matrixCalculator = new MatrixCalculator();

    public float getZPosition() {
        return zPosition;
    }

    public float getyPosition() {
        return yPosition;
    }

    public Vector getPosition() {
        return position;
    }

    public Tree(final Context context,
                final int textureResId3d,
                final int textureResId2d,
                final int shadowTextureResId,
                final Vector position, Course3DRenderer renderer) {
        this.position = position;
        position.z = ElevationHelper.getInstance().getHeightInPoint(this.position, renderer);
        if (polygonMap.get(textureResId3d) == null) {
            IndexedTexturedSquare polygon = new IndexedTexturedSquare(textureResId3d, context, 1.0f, false);
            polygonMap.put(textureResId3d, polygon);
        }

        if (polygonMap.get(shadowTextureResId) == null) {
            IndexedTexturedSquare polygon = new IndexedTexturedSquare(shadowTextureResId, context, 1.0f, false);
            polygonMap.put(shadowTextureResId, polygon);
        }

        if (polygonMap.get(textureResId2d) == null) {
            IndexedTexturedSquare polygon = new IndexedTexturedSquare(textureResId2d, context, 1.0f, false);
            polygonMap.put(textureResId2d, polygon);
        }

        tree3DPolygon = polygonMap.get(textureResId3d);
        tree2DPolygon = polygonMap.get(textureResId2d);
        shadowPolygon = polygonMap.get(shadowTextureResId);

        additionalRotation = random.nextInt(90);
    }

    public void destroy() {
    }

    public void calculateMatrices(final float x,
                                  final float y,
                                  final float z,
                                  final float viewAngle,
                                  final float rotationAngle,
                                  final float[] projectionMatrix) {
        matrixCalculator.calculateMatrices(x, y, z, viewAngle, rotationAngle, projectionMatrix);
    }

    public void drawShadow(Course3DRenderer renderer,
                           final float x,
                           final float y,
                           final float z,
                           final float rotationAngle,
                           final float viewAngle) {

        if (renderer.elevationDataExist && ElevationHelper.getInstance().tilesExist()) {

            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, z + 0.1f);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, (float) position.z);
            Matrix.scaleM(renderer.modelViewMatrix, 0, TreeSize, TreeSize, TreeSize);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1, 0f);

            renderer.bindMatrix();
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            renderer.enableDepth();
            if (Frustum.SphereInFrustum((float) position.x, (float) position.y, (float) position.z, 1f))
                shadowPolygon.draw(renderer, SHADOW_OPACITY);

            renderer.disableDepth();

        } else {

            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, TreeSize, z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, (float) position.z);
            Matrix.scaleM(renderer.modelViewMatrix, 0, TreeSize, TreeSize, TreeSize);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1, -1f);
            renderer.bindMatrix();
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            if (Frustum.SphereInFrustum((float) position.x, (float) position.y, (float) position.z, 1f))
                shadowPolygon.draw(renderer, SHADOW_OPACITY);

        }
    }


    public void drawShadowToFBO(Course3DRenderer renderer,
                                final float rotationAngle,
                                final float viewAngle,
                                final Vector tilePosition) {
        if (!Frustum.CubeInFrustum((float) position.x, (float) position.y, (float) 0, 1f)) {
            return;
        }

        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, TreeSize, (float) tilePosition.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, (float) tilePosition.x + (float) position.x, (float) tilePosition.y + (float) position.y, 0);
        Matrix.scaleM(renderer.modelViewMatrix, 0, TreeSize, TreeSize, TreeSize);
        // Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1, -1f);

        renderer.bindMatrix();
        shadowPolygon.draw(renderer, SHADOW_OPACITY);

    }


    public void draw(Course3DRenderer renderer,
                     final float x,
                     final float y,
                     final float z,
                     final float rotationAngle,
                     final float viewAngle) {
        if (renderer.elevationDataExist && ElevationHelper.getInstance().tilesExist()) {
            if (renderer.getNavigationMode() != Course3DRendererBase.NavigationMode.NavigationMode2D) {
                Matrix.setIdentityM(renderer.modelViewMatrix, 0);
                Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0f, z);
                Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
                Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
                Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, (float) position.z);
                Matrix.rotateM(renderer.modelViewMatrix, 0, 90, 1, 0, 0);// rotated to camera
                float angle = additionalRotation;
                if (enableAnimation) {
                    angle += (float) yrotator.getCurrentValue();
                }
                Matrix.rotateM(renderer.modelViewMatrix, 0, angle, 0, 1, 0);
                Matrix.scaleM(renderer.modelViewMatrix, 0, TreeSize, TreeSize, TreeSize);
                Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1f, 0);
                renderer.bindMatrix();
                renderer.enableDepth();
                if (Frustum.SphereInFrustum((float) position.x, (float) position.y, (float) position.z, TreeSize))
                    tree3DPolygon.drawArrays(renderer, renderer.DEFAULT_OPACITY);

                renderer.disableDepth();
            } else {
                Matrix.setIdentityM(renderer.modelViewMatrix, 0);
                Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, z);
                Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
                Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
                Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, 0);
                Matrix.scaleM(renderer.modelViewMatrix, 0, 0.5f, 0.5f, 0.5f);
                Matrix.rotateM(renderer.modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
                renderer.bindMatrix();
                tree2DPolygon.draw(renderer, renderer.DEFAULT_OPACITY);
            }
        } else {
            if (renderer.getNavigationMode() != Course3DRendererBase.NavigationMode.NavigationMode2D) {
                Matrix.setIdentityM(renderer.modelViewMatrix, 0);
                Matrix.translateM(renderer.modelViewMatrix, 0, 0, TreeSize, z);
                Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
                Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
                Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, (float) position.z);
                Matrix.rotateM(renderer.modelViewMatrix, 0, 90, 1, 0, 0);// rotated to camera
                float angle = additionalRotation;
                if (enableAnimation) {
                    angle += (float) yrotator.getCurrentValue();
                }
                Matrix.rotateM(renderer.modelViewMatrix, 0, angle, 0, 1, 0);
                Matrix.scaleM(renderer.modelViewMatrix, 0, TreeSize, TreeSize, TreeSize);
                renderer.bindMatrix();
                if (Frustum.SphereInFrustum((float) position.x, (float) position.y, (float) position.z, TreeSize))
                    tree3DPolygon.drawArrays(renderer, renderer.DEFAULT_OPACITY);
            } else {
                Matrix.setIdentityM(renderer.modelViewMatrix, 0);
                Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, z);
                Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
                Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
                Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, 0);
                Matrix.scaleM(renderer.modelViewMatrix, 0, 0.5f, 0.5f, 0.5f);
                Matrix.rotateM(renderer.modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
                renderer.bindMatrix();
                tree2DPolygon.draw(renderer, renderer.DEFAULT_OPACITY);
            }
        }
    }
}
