package com.l1inc.viewer.elevation;

import android.content.Context;
import android.opengl.ETC1Util;
import android.opengl.GLES20;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.drawing.BaseDrawingObject;
import com.l1inc.viewer.drawing.Constants;
import com.l1inc.viewer.drawing.Layer;
import com.l1inc.viewer.drawing.TextureCache;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.elevation.TileStaticData;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kirill Kartukov on 16.01.2018.
 */

public class GroundNode extends BaseDrawingObject {

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;
    private FloatBuffer textureBuffer;
    private int vertexCount;

    public static int textureId;


    private int mapHeight, mapWidth;

    private ArrayList<Vector> normals = new ArrayList<>();

    private float centerX;
    private float centerY;
    private float centerZ;
    private float radius;
    private float zHlfSize;
    private float yHlfSize;
    private float xHlfSize;
    private float nodeHeight;
    private float nodeWidth;

    private int startRow, startPos, endRow, endPos;

    private float nodeZheight;

    public GroundNode(Course3DRenderer renderer, int resId, Context context, int startRow, int endRow, int startPos, int endPos, float centerZ, float tileZheight) {


        this.startRow = startRow;
        this.startPos = startPos;
        this.endRow = endRow;
        this.endPos = endPos;
        this.nodeZheight = tileZheight;

        this.mapHeight = endRow - startRow;
        this.centerZ = centerZ;
        this.mapWidth = endPos - startPos;

        Vector position = VectorMath.multiplied(VectorMath.added(
                ElevationHelper.getInstance().getVectorList().get(endRow - 1).get(endPos - 1),
                ElevationHelper.getInstance().getVectorList().get(startRow).get(startPos)), 0.5);

        centerX = (float) position.x;
        centerY = (float) position.y;

        nodeHeight = Math.abs((float) ElevationHelper.getInstance().getVectorList().get(endRow - 1).get(startPos).y - (float) ElevationHelper.getInstance().getVectorList().get(startRow).get(startPos).y);
        nodeWidth = Math.abs((float) ElevationHelper.getInstance().getVectorList().get(startRow).get(endPos - 1).x - (float) ElevationHelper.getInstance().getVectorList().get(startRow).get(startPos).x);
        radius = Math.max(nodeHeight, nodeWidth);
        radius = Math.max(radius, tileZheight);

        radius /= 2;

        zHlfSize = tileZheight / 2;
        xHlfSize = nodeWidth / 2;
        yHlfSize = nodeHeight / 2;

        List<Float> vertexList = new ArrayList<>();
        List<Float> uvList = new ArrayList<>();

        float xC;
        float yC;
        float zC;

        vertexList.clear();
        for (int y = startRow; y < endRow; y++) {
            for (int x = startPos; x < endPos; x++) {

                yC = (float) Layer.transformLat((ElevationHelper.getInstance().getMaxLatitude() - ElevationHelper.getInstance().getStep() * (float) y));
                xC = (float) Layer.transformLon((ElevationHelper.getInstance().getMinLongitude() + ElevationHelper.getInstance().getStep() * (float) x));
                zC = (ElevationHelper.getInstance().getElevationArray().get(y).get(x) - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE;
                vertexList.add(xC);
                vertexList.add(yC);
                vertexList.add(zC);
                uvList.add(xC);
                uvList.add(yC);
            }
        }

        textureId = !ETC1Util.isETC1Supported() ? TextureCache.getTexture(context, resId) : TextureCache.getCompressedTexture(context, resId);
        vertexCount = vertexList.size();

        if (!TileStaticData.getInstance().indiciesExist(mapHeight, mapWidth)) {
            int indiciesLength = 3 * 2 * (mapWidth - 1) * (mapHeight - 1);
            TileStaticData.getInstance().indices = null;
            TileStaticData.getInstance().indices = new short[indiciesLength];

            TileStaticData.getInstance().indices = new short[3 * 2 * (mapWidth - 1) * (mapHeight - 1)];
            int vertIndex = 0;
            for (int i = 0; i <= mapHeight - 2; i++) {
                for (int j = 0; j <= mapWidth - 2; j++) {
                    int t = j + i * mapWidth;

                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + 1); // Upper Right
                    TileStaticData.getInstance().indices[vertIndex++] = (short) t; // Upper Left

                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + mapWidth); // Bottom Left
                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                    TileStaticData.getInstance().indices[vertIndex++] = (short) t; // Upper left

                }
            }

            TileStaticData.getInstance().indexBuffer = null;
            TileStaticData.getInstance().indexBuffer = ShortBuffer.allocate(TileStaticData.getInstance().indices.length * 2);
            for (short val : TileStaticData.getInstance().indices) {
                TileStaticData.getInstance().indexBuffer.put(val);
            }
            TileStaticData.getInstance().indexBuffer.position(0);
            TileStaticData.getInstance().addIndicies(mapHeight, mapWidth);
            TileStaticData.getInstance().indexBuffer.clear();
            TileStaticData.getInstance().indexBuffer = null;
            TileStaticData.getInstance().indices = null;

        }

        normals = ElevationHelper.getInstance().getNormalsArrayByPosition(startPos, startRow, endPos, endRow);

        int vs, ns;
        vs = vertexCount;
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        for (Float val : vertexList) {
            vertexBuffer.put(val);
        }

        vertexBuffer.position(0);

        vbb.clear();
        vertexList.clear();

        ns = vertexCount;
        ByteBuffer normalsByteBuffer = ByteBuffer.allocateDirect(vertexCount * 4);
        normalsByteBuffer.order(ByteOrder.nativeOrder());
        normalBuffer = normalsByteBuffer.asFloatBuffer();
        for (Vector val : normals) {
            normalBuffer.put((float) val.normalX);
            normalBuffer.put((float) val.normalY);
            normalBuffer.put((float) val.normalZ);
        }

        normalBuffer.position(0);

        normalsByteBuffer.clear();
        normals.clear();

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(uvList.size() * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        for (Float val : uvList) {
            textureBuffer.put(val);
        }
        textureBuffer.position(0);

        GLES20.glGenBuffers(1, vertexBuffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vs * 4, vertexBuffer.position(0), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glGenBuffers(1, normalsBuffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, ns * 4, normalBuffer.position(0), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glGenBuffers(1, textureBuffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, uvList.size() * 4, textureBuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glGenBuffers(1, indexBuffers, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, TileStaticData.getInstance().getIndicies(mapHeight, mapWidth).position(0).capacity(), TileStaticData.getInstance().getIndicies(mapHeight, mapWidth).position(0), GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private int[] vertexBuffers = new int[1];
    private int[] normalsBuffers = new int[1];
    private int[] textureBuffers = new int[1];
    private int[] indexBuffers = new int[1];

    public int[] getExtremePoints() {
        return new int[]{startRow, startPos, endRow, endPos};
    }

    public int[] getStartExtremePoints() {
        return new int[]{startRow, startPos};
    }

    public int[] getEndExtremePoints() {
        return new int[]{endRow, endPos};
    }

    public void draw(Course3DRenderer renderer, int groundpos) {
        if (Frustum.cubeInFrustum(centerX, centerY, centerZ, xHlfSize, yHlfSize, zHlfSize))
            drawWithBuffers(renderer, groundpos);
    }

    private void drawWithBuffers(Course3DRenderer renderer, int groundPos) {

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBuffers[0]);
        GLES20.glEnableVertexAttribArray(renderer.normalLocation);
        GLES20.glVertexAttribPointer(renderer.normalLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glEnableVertexAttribArray(renderer.shadowPositionLocation);
        GLES20.glVertexAttribPointer(renderer.shadowPositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBuffers[0]);
        GLES20.glEnableVertexAttribArray(renderer.shadowTexCoord);
        GLES20.glVertexAttribPointer(renderer.shadowTexCoord, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffers[0]);
        if (TileStaticData.getInstance().indiciesExist(mapHeight, mapWidth))
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, TileStaticData.getInstance().getIndicies(mapHeight, mapWidth).position(0).capacity() / 2, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    public void destroy(){
        if (normalsBuffers[0] != 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBuffers[0]);
            GLES20.glDeleteBuffers(1, normalsBuffers, 0);
            normalsBuffers[0] = 0;
        }
        if (vertexBuffers[0] != 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
            GLES20.glDeleteBuffers(1, vertexBuffers, 0);
            vertexBuffers[0] = 0;
        }
        if (textureBuffers[0] != 0) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, textureBuffers[0]);
            GLES20.glDeleteBuffers(1, textureBuffers, 0);
            textureBuffers[0] = 0;
        }
        if (normalsBuffers[0] != 0) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffers[0]);
            GLES20.glDeleteBuffers(1, normalsBuffers, 0);
            indexBuffers[0] = 0;
        }
    }

}
