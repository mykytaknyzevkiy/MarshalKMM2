package com.l1inc.viewer.elevation;

import android.opengl.GLES20;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.drawing.BaseDrawingObject;
import com.l1inc.viewer.drawing.Constants;
import com.l1inc.viewer.drawing.TextureCache;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Kirill Kartukov on 22.11.2017.
 */

public class Tile extends BaseDrawingObject {

    private class Texture {
        private int textureId;
        private boolean free = true;

        public int getTextureId() {
            return textureId;
        }

        public boolean isFree() {
            return free;
        }

        public void obtain() {
            free = false;
        }

        public void release() {
            free = true;
        }

        public Texture(final int textureId) {
            this.textureId = textureId;
            free = true;
        }

        @Override
        public boolean equals(Object obj) {
            return this.textureId == ((Texture) obj).textureId;
        }
    }

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalBuffer;

    private int mapHeight;
    private int mapWidth;

    private int indiciesLength;
    private int vertexCount;

    public float centerX;
    public float centerY;
    public float centerZ;
    public float centerZ3D;
    public float tileWidth;
    public float tileHeight;
    public float tileZHeight;
    public float cubeTileSize;

    /*private int frameBufferName = 0;
    private int renderedTexture = 0;*/

    private Vector firstVertexInFirstRow;
    private Vector firstVertexInLastRow;
    private Vector lastVertexInLastRow;
    private Vector lastVertexInFirstRow;

    public int viewPortWidth;
    public int viewPortHeight;

    public float zHlfSize;
    public float yHlfSize;
    public float xHlfSize;

    //private ArrayList<Float> normals;

    public int textureQuality = TileStaticData.TEXTURE_QUALITY_NONE;

    private ArrayList<Vector> normals = new ArrayList<>();

    //public List<Vector> vectorList2;

    private int startX;
    private int startY;
    private int endX;
    private int endY;

    public Tile(List<Float> vertexList, List<Vector> vectorList, int mapWidth, int mapHeight, int startX, int startY, int endX, int endY) {

        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        firstVertexInFirstRow = new Vector(vertexList.get(0), vertexList.get(1));
        firstVertexInLastRow = new Vector(vertexList.get(vertexList.size() - mapWidth * 3), vertexList.get(vertexList.size() - mapWidth * 3 + 1));//TODO
        lastVertexInFirstRow = new Vector(vertexList.get(mapWidth * 3 - 3), (vertexList.get(mapWidth * 3 - 2)));
        lastVertexInLastRow = new Vector(vertexList.get(vertexList.size() - 3), vertexList.get(vertexList.size() - 2));
        centerZ3D = ((ElevationHelper.getInstance().mMaxHeight - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE) / 2;
        tileHeight = Math.abs((float) firstVertexInLastRow.y - (float) firstVertexInFirstRow.y);
        tileWidth = Math.abs((float) lastVertexInFirstRow.x - (float) firstVertexInFirstRow.x);
        tileZHeight = Math.abs((ElevationHelper.getInstance().mMaxHeight - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE);


        cubeTileSize = Math.max(tileHeight, tileWidth);
        cubeTileSize = Math.max(cubeTileSize, tileZHeight);

        cubeTileSize /= 2;

        zHlfSize = tileZHeight / 2;
        xHlfSize = tileWidth / 2;
        yHlfSize = tileHeight / 2;

        Vector position = VectorMath.multiplied(VectorMath.added(lastVertexInLastRow, firstVertexInFirstRow), 0.5);

        firstVertexInFirstRow = null;
        firstVertexInLastRow = null;
        lastVertexInFirstRow = null;
        lastVertexInLastRow = null;


        centerX = -(float) position.x;
        centerY = -(float) position.y;
        centerZ = -(tileHeight / 2) * ((float) Math.tan((67.5) * Math.PI / 180));

        position = null;

        vertexCount = vertexList.size();
        indiciesLength = 3 * 2 * (mapWidth - 1) * (mapHeight - 1);
        Vector normal;

        if (!TileStaticData.getInstance().indiciesExist(mapHeight, mapWidth)) {
            TileStaticData.getInstance().indices = null;
            TileStaticData.getInstance().indices = new short[indiciesLength];
            int vertIndex = 0;

            for (int i = 0; i <= mapHeight - 2; i++) {
                for (int j = 0; j <= mapWidth - 2; j++) {
                    int t = j + i * mapWidth;
                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + 1); // Upper Right
                    TileStaticData.getInstance().indices[vertIndex++] = (short) t; // Upper Left

                    normal = VectorMath.getNormals2(vectorList.get(t + mapWidth + 1), vectorList.get(t + 1), vectorList.get(t));

                    vectorList.get(t + mapWidth + 1).setNormals(normal.x, normal.y, normal.z);
                    vectorList.get(t + 1).setNormals(normal.x, normal.y, normal.z);
                    vectorList.get(t).setNormals(normal.x, normal.y, normal.z);

                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + mapWidth); // Bottom Left
                    TileStaticData.getInstance().indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                    TileStaticData.getInstance().indices[vertIndex++] = (short) t; // Upper left

                    normal = VectorMath.getNormals2(vectorList.get(t + mapWidth), vectorList.get(t + mapWidth + 1), vectorList.get(t));

                    vectorList.get(t + mapWidth).setNormals(normal.x, normal.y, normal.z);
                    vectorList.get(t + mapWidth + 1).setNormals(normal.x, normal.y, normal.z);
                    vectorList.get(t).setNormals(normal.x, normal.y, normal.z);
                }
            }
            TileStaticData.getInstance().indexBuffer = null;
            TileStaticData.getInstance().indexBuffer = ShortBuffer.allocate(TileStaticData.getInstance().indices.length * 2 /*потому что short это два байта*/);
            for (short val : TileStaticData.getInstance().indices) {
                TileStaticData.getInstance().indexBuffer.put(val);
            }
            TileStaticData.getInstance().indexBuffer.position(0);
            TileStaticData.getInstance().addIndicies(mapHeight, mapWidth);
            TileStaticData.getInstance().indexBuffer.clear();
            TileStaticData.getInstance().indexBuffer = null;
            TileStaticData.getInstance().indices = null;
        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        for (Float val : vertexList) {
            vertexBuffer.put(val);
        }

        vertexBuffer.position(0);
        vertexBuffer.clear();

        vbb.clear();
        vertexList.clear();
        vectorList.clear();
        TileStaticData.getInstance().getIndicies(mapHeight, mapWidth).clear();
    }

    public synchronized void initNormals() {

        normals = ElevationHelper.getInstance().getNormalsArrayByPosition(startX, startY, endX, endY);
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
        normalBuffer.clear();

    }

    public synchronized boolean setTextureQuality(int quality, final int pos) {
        if (quality == textureQuality) {
            return false;
        }
        if (quality == TileStaticData.TEXTURE_QUALITY_NONE) {
            textureQuality = quality;
            destroy();
            return false;
        } else {
            if (quality < textureQuality && textureQuality != TileStaticData.TEXTURE_QUALITY_HIGH /* TODO REDUDANT && quality != TileStaticData.TEXTURE_QUALITY_NONE*/)
                return false;
            if (tileWidth > tileHeight) {
                viewPortWidth = (int) quality;
                viewPortHeight = (int) ((tileHeight * quality) / tileWidth);

            } else {
                viewPortHeight = (int) quality;
                viewPortWidth = (int) ((tileWidth * quality) / tileHeight);
            }
            textureQuality = quality;
            destroy();
            initiateFrameBuffer(pos);
            return true;
        }
    }

    private int[] frameB = new int[1];
    private Texture texture;
    private int index = 0;

    private static Map<Integer, Integer> framebufferMap = new HashMap<>();
    private static Map<Integer, List<Texture>> textureMap = new HashMap<>();

    public synchronized static void releaseTexturesAndFrameBuffers() {

        if (framebufferMap.containsKey(TileStaticData.TEXTURE_QUALITY_NONE)) {
            if (framebufferMap.get(TileStaticData.TEXTURE_QUALITY_NONE) != 0) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferMap.get(TileStaticData.TEXTURE_QUALITY_NONE));
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 0, 0);
                GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_RENDERBUFFER, 0);
                GLES20.glDeleteFramebuffers(1, new int[]{framebufferMap.get(TileStaticData.TEXTURE_QUALITY_NONE)}, 0);
            }
        }

        if (framebufferMap.containsKey(TileStaticData.TEXTURE_QUALITY_HIGH)) {
            if (framebufferMap.get(TileStaticData.TEXTURE_QUALITY_HIGH) != 0) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferMap.get(TileStaticData.TEXTURE_QUALITY_HIGH));
                GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 0, 0);
                GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_RENDERBUFFER, 0);
                GLES20.glDeleteFramebuffers(1, new int[]{framebufferMap.get(TileStaticData.TEXTURE_QUALITY_HIGH)}, 0);
            }
        }

        if (textureMap.containsKey(TileStaticData.TEXTURE_QUALITY_HIGH)) {
            final List<Texture> textureList = textureMap.get(TileStaticData.TEXTURE_QUALITY_HIGH);
            for (Texture texture : textureList) {
                if (texture.getTextureId() != 0) {
                    GLES20.glDeleteTextures(1, new int[]{texture.getTextureId()}, 0);

                    TextureCache.logDeallocatedTexture(texture.getTextureId());

                    texture.release();
                    texture.textureId = 0;
                }
            }

        }

        if (textureMap.containsKey(TileStaticData.TEXTURE_QUALITY_NONE)) {
            final List<Texture> textureList = textureMap.get(TileStaticData.TEXTURE_QUALITY_NONE);
            for (Texture texture : textureList) {
                if (texture.getTextureId() != 0) {
                    GLES20.glDeleteTextures(1, new int[]{texture.getTextureId()}, 0);

                    TextureCache.logDeallocatedTexture(texture.getTextureId());

                    texture.release();
                    texture.textureId = 0;
                }
            }
        }

        textureMap.clear();
        framebufferMap.clear();

    }

    private synchronized Texture obtainTexture() {
        if (!textureMap.containsKey(textureQuality)) {
            textureMap.put(textureQuality, new ArrayList<Texture>());
        }

        final List<Texture> textureList = textureMap.get(textureQuality);
        Texture freeTexture = null;
        for (int i = 0; i < textureList.size(); i++) {
            Texture texture = textureList.get(i);
            if (texture.isFree()) {
                freeTexture = texture;
                index = i;
                break;
            }
        }

        if (freeTexture == null) {
            final int[] arr = new int[1];

            GLES20.glGenTextures(1, arr, 0);

            TextureCache.logAllocatedTexture(arr[0]);

            //GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameB[0]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, arr[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, viewPortWidth, viewPortHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            freeTexture = new Texture(arr[0]);
            textureList.add(freeTexture);
            index = textureList.size() - 1;

//            Course3DRenderer.getViewerLogger().info(
//                    String.format(Locale.US,
//                            "_rendering_ obtainTexture added texture %dx%d id: %d thread id: %d thread name: %s",
//                            viewPortWidth,
//                            viewPortHeight,
//                            freeTexture.getTextureId(),
//                            Thread.currentThread().getId(),
//                            Thread.currentThread().getName())
//            );
            //Log.e("TILE ", String.format("added texture %dx%d id: %d", viewPortWidth, viewPortHeight, freeTexture.getTextureId()));
        }

        freeTexture.obtain();
        return freeTexture;
    }

    public synchronized void initiateFrameBuffer(int pos) {

        if (texture != null && texture.getTextureId() != 0) {
            // Log.e("TILE", "cant initialize frame buffer for pos : " + pos + "  ,renderedTexture " + frameB[0] + " ,renderedTexture " + texture.getTextureId());
            return;
        }

        frameB = new int[1];

        if (!framebufferMap.containsKey(textureQuality)) {
            GLES20.glGenFramebuffers(1, frameB, 0);

            framebufferMap.put(textureQuality, frameB[0]);

//            Course3DRenderer.getViewerLogger().info(
//                    String.format(Locale.US,
//                            "_rendering_ initiateFrameBuffer texture %dx%d id: %d thread id: %d thread name: %s",
//                            viewPortWidth,
//                            viewPortHeight,
//                            frameB[0],
//                            Thread.currentThread().getId(),
//                            Thread.currentThread().getName())
//            );

            // Log.e("TILE ", String.format("added framebuffer %dx%d id:", viewPortWidth, viewPortHeight, frameB[0]));
        } else {
            frameB[0] = framebufferMap.get(textureQuality);
        }

        texture = obtainTexture();

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
        }

    }

    public synchronized void prepareForRenderToTexture() {
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture.getTextureId(), 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    public synchronized boolean draw(Course3DRenderer renderer, int pos) {

        if (!Frustum.cubeInFrustum(-centerX, -centerY, centerZ3D, cubeTileSize, xHlfSize, yHlfSize, zHlfSize)) {
            return true;
        }

        try {
            if (texture == null) {
                textureQuality = TileStaticData.TEXTURE_QUALITY_NONE;
                return false;
            }

            if (texture.getTextureId() == 0) {
                textureQuality = TileStaticData.TEXTURE_QUALITY_NONE;
                return false;
            }

            if (textureMap.get(textureQuality) == null) {
                textureQuality = TileStaticData.TEXTURE_QUALITY_NONE;
                return false;
            }
            if (index >= textureMap.get(textureQuality).size()) {
                textureQuality = TileStaticData.TEXTURE_QUALITY_NONE;
                return false;
            }
            if (!textureMap.get(textureQuality).get(index).equals(texture)) {
                textureQuality = TileStaticData.TEXTURE_QUALITY_NONE;
                return false;
            }
        } catch (Exception e) {
            textureQuality = TileStaticData.TEXTURE_QUALITY_NONE;
            return false;
        }

        GLES20.glBindTexture(GL10.GL_TEXTURE_2D, texture.getTextureId());
        GLES20.glVertexAttribPointer(renderer.shadowPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(renderer.shadowTexCoord, 2, GLES20.GL_FLOAT, false, 0, TileStaticData.getInstance().getUVList(mapHeight, mapWidth).position(0));
        GLES20.glEnableVertexAttribArray(renderer.shadowTexCoord);
        GLES20.glEnableVertexAttribArray(renderer.shadowPositionLocation);
        GLES20.glVertexAttribPointer(renderer.normalLocation, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
        GLES20.glEnableVertexAttribArray(renderer.normalLocation);
        if (TileStaticData.getInstance().getIndicies(mapHeight, mapWidth) != null)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indiciesLength, GL10.GL_UNSIGNED_SHORT, TileStaticData.getInstance().getIndicies(mapHeight, mapWidth).position(0));

        vertexBuffer.clear();
        normalBuffer.clear();
        TileStaticData.getInstance().getIndicies(mapHeight, mapWidth).clear();
        return true;
    }

    public float getCenterX() {
        return centerX;
    }


    public float getCenterY() {
        return centerY;
    }

    public float getCenterZ() {
        return centerZ;
    }

    public Vector getTilePosition2D() {
        return new Vector(centerX, centerY, centerZ);
    }

    public int getFboId() {
        return frameB[0];
    }

    public int getTextureQuality() {
        return textureQuality;
    }

    public int getViewPortWidth() {
        return viewPortWidth;
    }

    public int getViewPortHeight() {
        return viewPortHeight;
    }

    public synchronized void destroy() {
        if (texture != null) {
            texture.release();
            texture = null;
        }
    }

    public void setViewPortWidth(int viewPortWidth) {
        this.viewPortWidth = viewPortWidth;
    }

    public void setViewPortHeight(int viewPortHeight) {
        this.viewPortHeight = viewPortHeight;
    }
}
