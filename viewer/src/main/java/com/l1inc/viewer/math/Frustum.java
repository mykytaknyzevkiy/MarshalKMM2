package com.l1inc.viewer.math;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.drawing.Layer;
import com.l1inc.viewer.drawing.LayerPolygon;
import com.l1inc.viewer.drawing.PointList;
import com.l1inc.viewer.drawing.PointListLayer;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.elevation.Tile;

import java.util.ArrayList;

/**
 * Created by Kirill Kartukov on 26.01.2018.
 */

public class Frustum {

    private static float[][] frustum = new float[6][4];

    public static void updateFrustum(Course3DRenderer renderer) {
        updateFrustum(renderer.modelViewMatrix, renderer.projectionMatrix);
    }

    public static void updateFrustum(float[] modelViewMatrix, float[] projectionMatrix) {
        float[] proj = new float[16];
        float[] modl = new float[16];
        float[] clip = new float[16];
        float t;

        proj = projectionMatrix;
        modl = modelViewMatrix;

        /* Get the current PROJECTION matrix from OpenGL */
        /* Get the current MODELVIEW matrix from OpenGL */
        /* Combine the two matrices (multiply projection by modelview)    */
        clip[0] = modl[0] * proj[0] + modl[1] * proj[4] + modl[2] * proj[8] + modl[3] * proj[12];
        clip[1] = modl[0] * proj[1] + modl[1] * proj[5] + modl[2] * proj[9] + modl[3] * proj[13];
        clip[2] = modl[0] * proj[2] + modl[1] * proj[6] + modl[2] * proj[10] + modl[3] * proj[14];
        clip[3] = modl[0] * proj[3] + modl[1] * proj[7] + modl[2] * proj[11] + modl[3] * proj[15];
        clip[4] = modl[4] * proj[0] + modl[5] * proj[4] + modl[6] * proj[8] + modl[7] * proj[12];
        clip[5] = modl[4] * proj[1] + modl[5] * proj[5] + modl[6] * proj[9] + modl[7] * proj[13];
        clip[6] = modl[4] * proj[2] + modl[5] * proj[6] + modl[6] * proj[10] + modl[7] * proj[14];
        clip[7] = modl[4] * proj[3] + modl[5] * proj[7] + modl[6] * proj[11] + modl[7] * proj[15];
        clip[8] = modl[8] * proj[0] + modl[9] * proj[4] + modl[10] * proj[8] + modl[11] * proj[12];
        clip[9] = modl[8] * proj[1] + modl[9] * proj[5] + modl[10] * proj[9] + modl[11] * proj[13];
        clip[10] = modl[8] * proj[2] + modl[9] * proj[6] + modl[10] * proj[10] + modl[11] * proj[14];
        clip[11] = modl[8] * proj[3] + modl[9] * proj[7] + modl[10] * proj[11] + modl[11] * proj[15];
        clip[12] = modl[12] * proj[0] + modl[13] * proj[4] + modl[14] * proj[8] + modl[15] * proj[12];
        clip[13] = modl[12] * proj[1] + modl[13] * proj[5] + modl[14] * proj[9] + modl[15] * proj[13];
        clip[14] = modl[12] * proj[2] + modl[13] * proj[6] + modl[14] * proj[10] + modl[15] * proj[14];
        clip[15] = modl[12] * proj[3] + modl[13] * proj[7] + modl[14] * proj[11] + modl[15] * proj[15];
        /* Extract the numbers for the RIGHT plane */
        frustum[0][0] = clip[3] - clip[0];
        frustum[0][1] = clip[7] - clip[4];
        frustum[0][2] = clip[11] - clip[8];
        frustum[0][3] = clip[15] - clip[12];
        /* Normalize the result */
        t = (float) Math.sqrt(frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2]);
        frustum[0][0] /= t;
        frustum[0][1] /= t;
        frustum[0][2] /= t;
        frustum[0][3] /= t;
        /* Extract the numbers for the LEFT plane */
        frustum[1][0] = clip[3] + clip[0];
        frustum[1][1] = clip[7] + clip[4];
        frustum[1][2] = clip[11] + clip[8];
        frustum[1][3] = clip[15] + clip[12];
        /* Normalize the result */
        t = (float) Math.sqrt(frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2]);
        frustum[1][0] /= t;
        frustum[1][1] /= t;
        frustum[1][2] /= t;
        frustum[1][3] /= t;
        /* Extract the BOTTOM plane */
        frustum[2][0] = clip[3] + clip[1];
        frustum[2][1] = clip[7] + clip[5];
        frustum[2][2] = clip[11] + clip[9];
        frustum[2][3] = clip[15] + clip[13];
        /* Normalize the result */
        t = (float) Math.sqrt(frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2]);
        frustum[2][0] /= t;
        frustum[2][1] /= t;
        frustum[2][2] /= t;
        frustum[2][3] /= t;
        /* Extract the TOP plane */
        frustum[3][0] = clip[3] - clip[1];
        frustum[3][1] = clip[7] - clip[5];
        frustum[3][2] = clip[11] - clip[9];
        frustum[3][3] = clip[15] - clip[13];
        /* Normalize the result */
        t = (float) Math.sqrt(frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2]);
        frustum[3][0] /= t;
        frustum[3][1] /= t;
        frustum[3][2] /= t;
        frustum[3][3] /= t;
        /* Extract the FAR plane */
        frustum[4][0] = clip[3] - clip[2];
        frustum[4][1] = clip[7] - clip[6];
        frustum[4][2] = clip[11] - clip[10];
        frustum[4][3] = clip[15] - clip[14];
        /* Normalize the result */
        t = (float) Math.sqrt(frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2]);
        frustum[4][0] /= t;
        frustum[4][1] /= t;
        frustum[4][2] /= t;
        frustum[4][3] /= t;
        /* Extract the NEAR plane */
        frustum[5][0] = clip[3] + clip[2];
        frustum[5][1] = clip[7] + clip[6];
        frustum[5][2] = clip[11] + clip[10];
        frustum[5][3] = clip[15] + clip[14];
        /* Normalize the result */
        t = (float) Math.sqrt(frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2]);
        frustum[5][0] /= t;
        frustum[5][1] /= t;
        frustum[5][2] /= t;
        frustum[5][3] /= t;
    }


    public static boolean isPointInFrustum(Vector vector) {
        int p;
        for (p = 0; p < 6; p++)
            if (frustum[p][0] * vector.x + frustum[p][1] * vector.y + frustum[p][2] * vector.z + frustum[p][3] <= 0)
                return false;
        return true;
    }

    public static boolean isRectangleInFrustum(Vector bottomLeftDown,
                                               Vector bottomRightDown,
                                               Vector bottomLeftUP,
                                               Vector bottomRightUp,
                                               Vector topLeftDown,
                                               Vector topRightDown,
                                               Vector topLeftUp,
                                               Vector topRightUp) {
        int p;
        for (p = 0; p < 6; p++) {
            if (frustum[p][0] * (topLeftDown.x) + frustum[p][1] * (topLeftDown.y) + frustum[p][2] * (topLeftDown.z) + frustum[p][3] > 0)   // BOTTOM LEFT DOWN
                continue;
            if (frustum[p][0] * (topRightDown.x) + frustum[p][1] * (topRightDown.y) + frustum[p][2] * (topRightDown.z) + frustum[p][3] > 0)   // BOTTOM RIGHT DOWN
                continue;
            if (frustum[p][0] * (topLeftUp.z) + frustum[p][1] * (topLeftUp.y) + frustum[p][2] * (topLeftUp.z) + frustum[p][3] > 0)   // BOTTOM LEFT UP
                continue;
            if (frustum[p][0] * (topRightUp.x) + frustum[p][1] * (topRightUp.y) + frustum[p][2] * (topRightUp.z) + frustum[p][3] > 0)   // BOTTOM RIGHT UP
                continue;
            if (frustum[p][0] * (bottomLeftDown.x) + frustum[p][1] * (bottomLeftDown.y) + frustum[p][2] * (bottomLeftDown.z) + frustum[p][3] > 0)   // TOP LEFT DOWN
                continue;
            if (frustum[p][0] * (bottomRightDown.x) + frustum[p][1] * (bottomRightDown.y) + frustum[p][2] * (bottomRightDown.z) + frustum[p][3] > 0)   // TOP RIGHT DOWN
                continue;
            if (frustum[p][0] * (bottomLeftUP.x) + frustum[p][1] * (bottomLeftUP.y) + frustum[p][2] * (bottomLeftUP.z) + frustum[p][3] > 0)   // TOP LEFT  UP
                continue;
            if (frustum[p][0] * (bottomRightUp.x) + frustum[p][1] * (bottomRightUp.y) + frustum[p][2] * (bottomRightUp.z) + frustum[p][3] > 0)   // TOP RIGHT  UP
                continue;
            return false;
        }
        return true;
    }

   /* public boolean isRectangleInFrustum(Vector bottomLeftDown,
                                        Vector bottomRightDown,
                                        Vector bottomLeftUP,
                                        Vector bottomRightUp) {
        int p =3;
        //for (p = 0; p < 6; p++) {
        if (frustum[p][0] * (bottomLeftDown.x) + frustum[p][1] * (bottomLeftDown.y) + frustum[p][2] * (bottomLeftDown.z) + frustum[p][3] > 0)   // BOTTOM LEFT DOWN
            return true;
        if (frustum[p][0] * (bottomRightDown.x) + frustum[p][1] * (bottomRightDown.y) + frustum[p][2] * (bottomRightDown.z) + frustum[p][3] > 0)   // BOTTOM RIGHT DOWN
            return true;
        if (frustum[p][0] * (bottomLeftUP.z) + frustum[p][1] * (bottomLeftUP.y) + frustum[p][2] * (bottomLeftUP.z) + frustum[p][3] > 0)   // BOTTOM LEFT UP
            return true;
        if (frustum[p][0] * (bottomRightUp.x) + frustum[p][1] * (bottomRightUp.y) + frustum[p][2] * (bottomRightUp.z) + frustum[p][3] > 0)   // BOTTOM RIGHT UP
            return true;
            /*if (frustum[p][0] * (topLeftDown.x) + frustum[p][1] * (topLeftDown.y) + frustum[p][2] * (topLeftDown.z) + frustum[p][3] > 0)   // TOP LEFT DOWN
                continue;
            if (frustum[p][0] * (topRightDown.x) + frustum[p][1] * (topRightDown.y) + frustum[p][2] * (topRightDown.z) + frustum[p][3] > 0)   // TOP RIGHT DOWN
                continue;
            if (frustum[p][0] * (topLeftUp.x) + frustum[p][1] * (topLeftUp.y) + frustum[p][2] * (topLeftUp.z) + frustum[p][3] > 0)   // TOP LEFT  UP
                continue;
            if (frustum[p][0] * (topRightUp.x) + frustum[p][1] * (topRightUp.y) + frustum[p][2] * (topRightUp.z) + frustum[p][3] > 0)   // TOP RIGHT  UP
                continue;*/
    //return false;
    //}
    //return true;
    //}*/


    public static boolean CubeInFrustum(float x, float y, float z, float size) {
        int p;
        for (p = 0; p < 6; p++) {
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y - size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y - size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y + size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y + size) + frustum[p][2] * (z - size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y - size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y - size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - size) + frustum[p][1] * (y + size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + size) + frustum[p][1] * (y + size) + frustum[p][2] * (z + size) + frustum[p][3] > 0)
                continue;
            return false;
        }
        return true;
    }

    public static boolean cubeInFrustum(float x, float y, float z, float size, float xHalfSize, float yHalfSize, float zHalfSize) {
        int p;
        for (p = 0; p < 6; p++) {
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            return false;
        }
        return true;
    }

    public static boolean cubeInFrustum(Tile tile) {
        float x = -tile.getCenterX();
        float y = -tile.getCenterY();
        float z = tile.centerZ3D;
        float xHalfSize = tile.xHlfSize;
        float yHalfSize = tile.yHlfSize;
        float zHalfSize = tile.zHlfSize;
        int p;
        for (p = 0; p < 6; p++) {
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            return false;
        }
        return true;
    }

    public static boolean cubeInFrustum(float x, float y, float z, float xHalfSize, float yHalfSize, float zHalfSize) {
        int p;
        for (p = 0; p < 6; p++) {
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z - zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y - yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x - xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            if (frustum[p][0] * (x + xHalfSize) + frustum[p][1] * (y + yHalfSize) + frustum[p][2] * (z + zHalfSize) + frustum[p][3] > 0)
                continue;
            return false;
        }
        return true;
    }

    public static boolean SphereInFrustum(float x, float y, float z, float radius) {
        int p;
        for (p = 0; p < 6; p++)
            if (frustum[p][0] * x + frustum[p][1] * y + frustum[p][2] * z + frustum[p][3] <= -radius)
                return false;
        return true;
    }

    public static boolean isPolygonInFrustum(ArrayList<Vector> vertexList) {
        int f, p;
        for (f = 0; f < 6; f++) {
            for (p = 0; p < vertexList.size(); p++) {
                if (frustum[f][0] * vertexList.get(p).x + frustum[f][1] * vertexList.get(p).y + frustum[f][2] * vertexList.get(p).z + frustum[f][3] > 0)
                    break;
            }
            if (p == vertexList.size())
                return false;
        }
        return true;
    }

    public static boolean isArrayInFrustum(Vector[] vertexList, Course3DRenderer renderer) {
        int f, p;
        for (f = 0; f < 6; f++) {
            for (p = 0; p < vertexList.length; p++) {
                vertexList[p].z = ElevationHelper.getInstance().getHeightInPoint(vertexList[p], renderer);
                if (frustum[f][0] * vertexList[p].x + frustum[f][1] * vertexList[p].y + frustum[f][2] * vertexList[p].z + frustum[f][3] > 0)
                    break;
            }
            if (p == vertexList.length)
                return false;
        }
        return true;
    }

    public static boolean isLayerInFrustum(Layer layer, Course3DRenderer renderer) {
        if (layer == null || layer.getLayerPolygons() == null)
            return false;

        for (LayerPolygon layerPolygon : layer.getLayerPolygons()) {
            for (Vector vector : layerPolygon.getPointList()) {
                Vector v1 = new Vector(vector.copy());
                v1.z = ElevationHelper.getInstance().getHeightInPoint(v1, renderer);
                if (!isPointInFrustum(v1)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isLayerInFrustum(PointListLayer layer, Course3DRenderer renderer) {
        if (layer == null || layer.getPointList() == null)
            return false;

        for (PointList pointList : layer.getPointList()) {
            for (Vector vector : pointList.getPointList()) {
                Vector v1 = new Vector(vector.copy());
                v1.z = ElevationHelper.getInstance().getHeightInPoint(v1, renderer);
                if (!isPointInFrustum(v1)) {
                    return false;
                }
            }
        }

        return true;
    }

}
