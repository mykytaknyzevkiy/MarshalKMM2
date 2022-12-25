package com.l1inc.viewer.drawing;

import android.opengl.GLES20;

import com.l1inc.viewer.math.Vector;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Yevhen Paschenko on 4/6/2016.
 */
public class DrawElement {

    private int type;
    private float[] pointList;
    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer textureCoordinates;
    private int vboId;
    private int texId;
    private int numPolygons;

    private ArrayList<Vector> vertexList = new ArrayList<>();

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float[] getPointList() {
        return pointList;
    }

    public void setPointList(float[] pointList) {
        this.pointList = pointList;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(FloatBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public FloatBuffer getTextureCoordinates() {
        return textureCoordinates;
    }

    public void setTextureCoordinates(FloatBuffer textureCoordinates) {
        this.textureCoordinates = textureCoordinates;
    }

    public int getVboId() {
        return vboId;
    }

    public int getTexId() {
        return texId;
    }

    public int getNumPolygons() {
        return numPolygons;
    }

    public void setNumPolygons(int numPolygons) {
        this.numPolygons = numPolygons;
    }

    public ArrayList<Vector> getVertexList() {
        return vertexList;
    }

    public void setVertexList(ArrayList<Vector> vertexList) {
        this.vertexList = vertexList;
    }

    public FloatBuffer getNormalBuffer() {
        return normalBuffer;
    }

    public void setNormalBuffer(FloatBuffer normalBuffer) {
        this.normalBuffer = normalBuffer;
    }

//    public void buildVbo() {
//
//        final int buffers[] = new int[2];
//        GLES20.glGenBuffers(2, buffers, 0);
//
//        vboId = buffers[0];
//        texId = buffers[1];
//
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, texId);
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureCoordinates.capacity() * 4, textureCoordinates, GLES20.GL_STATIC_DRAW);
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
//    }

    public void destroy() {
        GLES20.glDeleteBuffers(2, new int[]{0, texId}, 0);
    }
}
