package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.math.Interpolator;
import com.l1inc.viewer.math.PolygonOffsetter;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;
import com.l1inc.viewer.parcer.Shape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kirill Kartukov on 05.04.2018.
 */

public class Bunker3DPolygon extends BaseDrawingObject {

    private int textureId = -1;
    private RectF boundingBox;
    private Vector extremeLeft;
    private Vector extremeTop;
    private Vector extremeRight;
    private Vector extremeBottom;
    private DrawElement[] drawElements;
    private short[] indicies;

    private int textureResId = -1;
    private Context context;
    private boolean isTextureCompressed = false;

    private double bunkerDepth = 0.6 / Constants.LOCATION_SCALE;

    public FloatBuffer vertexBuffer;

    private String layerName = "";

    private FloatBuffer normalBuffer;
    public ShortBuffer indexBuffer;
    public FloatBuffer textureBuffer;

    private int[] vertexBuffers = new int[1];
    private int[] normalsBuffers = new int[1];
    private int[] textureBuffers = new int[1];
    private int[] indexBuffers = new int[1];

    private Float minLayerAltitude;
    private Course3DRenderer renderer;

    private ArrayList<ArrayList<Vector>> scaledArrays;

    private ArrayList<Integer> pointsToRemove = new ArrayList<>();

    private List<Vector> rawPointList = new ArrayList<>();

    public RectF getBoundingBox() {
        return boundingBox;
    }

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

    private void addLog(String mes) {
        Log.e(getClass().getSimpleName(), mes);
    }

    int indexCount;


    public Bunker3DPolygon(final String layerName,
                           final Shape shape,
                           final Context context,
                           final int textureResId,
                           final float extension,
                           final Layer.PointInterpolation interpolation,
                           final boolean isTextureCompressed,
                           IDrawable parent, Course3DRenderer renderer) throws Exception {

        this.isTextureCompressed = isTextureCompressed;
        this.context = context;
        this.textureResId = textureResId;

        try {


            final String points = shape.getPoints();
            this.layerName = layerName;
            this.renderer = renderer;
            final String[] lonLatPointsArray = points.split(",");
            final String lonLatPoints1 = lonLatPointsArray[0];
            final String[] lonLatPair1 = lonLatPoints1.split(" ");
            final float lat1 = (float) (Layer.transformLat(lonLatPair1[1]));
            final float lon1 = (float) (Layer.transformLon(lonLatPair1[0]));
            minLayerAltitude = ElevationHelper.getInstance().getHeightInPoint(lon1, lat1, renderer);

            for (int i = 0; i < lonLatPointsArray.length; i++) {
                final String lonLatPoints = lonLatPointsArray[i];
                final String[] lonLatPair = lonLatPoints.split(" ");
                final float lat = (float) (Layer.transformLat(lonLatPair[1]));
                final float lon = (float) (Layer.transformLon(lonLatPair[0]));
                Vector newVector = new Vector(lon, lat);
                if (rawPointList.size() > 0) {
                    final Vector prev = rawPointList.get(rawPointList.size() - 1);
                    if (prev.equals(newVector)) {
                        continue;
                    }
                }
                minLayerAltitude = Math.min(minLayerAltitude, ElevationHelper.getInstance().getHeightInPoint(newVector, renderer));
                rawPointList.add(newVector);
            }
            if (rawPointList.get(rawPointList.size() - 1).equals(rawPointList.get(0))) {
                rawPointList.remove(rawPointList.size() - 1);
            }

            VectorMath.VectorOrder vectorOrder = VectorMath.getVectorOrder(rawPointList);

            if (vectorOrder == VectorMath.VectorOrder.CW) {
                Collections.reverse(rawPointList);
            }

            if (interpolation == Layer.PointInterpolation.Interpolate) {
                rawPointList = new ArrayList<>(Interpolator.interpolate(rawPointList, 3, Interpolator.CatmullRomType.Centripetal));
            }

            if (extension > 0) {
                rawPointList = PolygonOffsetter.extendPolygon(rawPointList, extension);
            }

            if (rawPointList.get(rawPointList.size() - 1).equals(rawPointList.get(0))) {
                rawPointList.remove(rawPointList.size() - 1);
            }

            rawPointList = normalizeVectorList(rawPointList);

            /*
            Sometimes resizing algorithm creates NaN x,y  values, in order to prevent crashes we need to check if resized array has Vectors with NaN values,
            which we will skip later
            */

            pointsToRemove = findNaNValues(rawPointList);

            loadTexture();

            for (int i = 0; i < rawPointList.size(); i++) {
                final float lon = (float) rawPointList.get(i).x;
                final float lat = (float) rawPointList.get(i).y;

                if (boundingBox == null) {
                    boundingBox = new RectF(lon, lat, lon, lat);
                } else {
                    boundingBox.union(lon, lat);
                }

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

            createBunkers();

            BunkerTriangulator bunkerTriangulator = new BunkerTriangulator(scaledArrays);

            bunkerTriangulator.triangulate();

            List<Vector> bottom = scaledArrays.get(scaledArrays.size() - 1);

            float[] vertices = new float[bottom.size() * 3 + 3];

            int k = 0;
            for (Vector v : bottom) {
                vertices[k++] = (float) v.x;
                vertices[k++] = (float) v.y;
                vertices[k++] = (float) v.z;
            }

            vertices[k++] = (float) bottom.get(0).x;
            vertices[k++] = (float) bottom.get(0).y;
            vertices[k++] = (float) bottom.get(0).z;

            drawElements = LayerPolygon.triangulate(vertices);


            Float baseU = null;
            Float baseV = null;
            for (final DrawElement element : drawElements) {
                final ByteBuffer vertexbb = ByteBuffer.allocateDirect(element.getPointList().length * 4);
                vertexbb.order(ByteOrder.nativeOrder());


                final ByteBuffer normalbb = ByteBuffer.allocateDirect(element.getPointList().length * 4);
                normalbb.order(ByteOrder.nativeOrder());

                float[] normalList = new float[element.getPointList().length];

                for (int i = 0; i < element.getPointList().length / 3; i++) {
                    normalList[i * 3] = 0f;
                    normalList[i * 3 + 1] = 0f;
                    normalList[i * 3 + 2] = 1f;
                }

                /*float[] normalList = new float[bunkerTriangulator.normalListBottom.size()];

                for(int i = 0;i < bunkerTriangulator.normalListBottom.size();i++){
                    normalList[i] = bunkerTriangulator.normalListBottom.get(i);
                }*/

                element.setNormalBuffer(normalbb.asFloatBuffer());
                element.getNormalBuffer().put(normalList);
                element.getNormalBuffer().position(0);

                element.setVertexBuffer(vertexbb.asFloatBuffer());
                element.getVertexBuffer().put(element.getPointList());
                element.getVertexBuffer().position(0);
                for (int i = 0; i < element.getPointList().length; i += 3) {
                    element.getVertexList().add(new Vector(element.getPointList()[i], element.getPointList()[i + 1], element.getPointList()[i + 2]));
                }
                final float[] texCoords = new float[element.getPointList().length / 3 * 2];
                for (int i = 0; i < element.getPointList().length / 3; i++) {
                    if (baseU == null || baseV == null) {
                        baseU = element.getPointList()[i * 3];
                        baseV = element.getPointList()[i * 3 + 1];
                    }

                    texCoords[i * 2] = element.getPointList()[i * 3] - baseU;
                    texCoords[i * 2 + 1] = element.getPointList()[i * 3 + 1] - baseV;
                }

                final ByteBuffer textbb = ByteBuffer.allocateDirect(texCoords.length * 4);
                textbb.order(ByteOrder.nativeOrder());
                element.setTextureCoordinates(textbb.asFloatBuffer());
                element.getTextureCoordinates().put(texCoords);
                element.getTextureCoordinates().position(0);
                element.setNumPolygons(element.getPointList().length / 3);
                element.setPointList(null);
            }


            ByteBuffer vbb = ByteBuffer.allocateDirect(bunkerTriangulator.vertexList.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            vertexBuffer = vbb.asFloatBuffer();
            for (Float val : bunkerTriangulator.vertexList) {
                vertexBuffer.put(val);


            }

            vertexBuffer.position(0);

            ByteBuffer nbb = ByteBuffer.allocateDirect(bunkerTriangulator.normalList.length * 4);
            nbb.order(ByteOrder.nativeOrder());
            normalBuffer = nbb.asFloatBuffer();
            for (Float val : bunkerTriangulator.normalList) {
                normalBuffer.put(val);
            }

            normalBuffer.position(0);

            indexCount = bunkerTriangulator.indexList.length;
            indexBuffer = ShortBuffer.allocate(bunkerTriangulator.indexList.length * 2).put(bunkerTriangulator.indexList);
            indexBuffer.position(0);

            ByteBuffer byteBuf = ByteBuffer.allocateDirect(bunkerTriangulator.uvList.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            textureBuffer = byteBuf.asFloatBuffer();
            for (Float val : bunkerTriangulator.uvList) {
                textureBuffer.put(val);
            }
            textureBuffer.position(0);


            GLES20.glGenBuffers(1, vertexBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bunkerTriangulator.vertexList.length * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glGenBuffers(1, normalsBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bunkerTriangulator.normalList.length * 4, normalBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glGenBuffers(1, textureBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bunkerTriangulator.uvList.length * 4, textureBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glGenBuffers(1, indexBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, bunkerTriangulator.indexList.length * 2, indexBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTexture() {
        if (!isTextureCompressed)
            textureId = TextureCache.getTexture(context, textureResId);
        else
            textureId = !ETC1Util.isETC1Supported() ? TextureCache.getTexture(context, textureResId) : TextureCache.getCompressedTexture(context, textureResId);
    }

    public void draw(Course3DRenderer renderer) {
        if (textureId == -1 || !TextureCache.hasTexture(textureResId,textureId)) {
            loadTexture();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);


        for (final DrawElement element : drawElements) {

            if (element.getVboId() != 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, element.getTexId());
                GLES20.glClear(0);
                GLES20.glVertexAttribPointer(renderer.shadowTexCoord, 2, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, element.getVboId());
                GLES20.glVertexAttribPointer(renderer.shadowPositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glVertexAttribPointer(renderer.normalLocation, 3, GLES20.GL_FLOAT, false, 0, element.getNormalBuffer());
                GLES20.glDrawArrays(element.getType(), 0, element.getNumPolygons());
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            } else {
                GLES20.glVertexAttribPointer(renderer.shadowPositionLocation, 3, GLES20.GL_FLOAT, false, 0, element.getVertexBuffer());
                GLES20.glVertexAttribPointer(renderer.shadowTexCoord, 2, GLES20.GL_FLOAT, false, 0, element.getTextureCoordinates());
                GLES20.glVertexAttribPointer(renderer.normalLocation, 3, GLES20.GL_FLOAT, false, 0, element.getNormalBuffer());
                GLES20.glDrawArrays(element.getType(), 0, element.getNumPolygons());
            }
        }

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
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);


    }

    public void destroy() {
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

        for (final DrawElement element : drawElements) {
            element.destroy();
        }

        drawElements = new DrawElement[0];
    }

    public float getZPositionForPoint(float scaleIndex, Vector point, float baseAltitude) {
        if (scaleIndex == 0)
            return ElevationHelper.getInstance().getHeightInPoint(point, renderer);
        else {
            double diff = baseAltitude - (baseAltitude - bunkerDepth);
            double k = scaleIndex / 10;
            return (float) (baseAltitude - diff * Math.sqrt(k));
        }
    }

    public ArrayList<Vector> resizePolygon(List<Vector> rawPointList, double offset) {
        ArrayList<Vector> retVal = new ArrayList<>();

        int n = rawPointList.size();

        double mi;
        double mi1;
        double li;
        double li1;
        double ri;
        double ri1;
        double si;
        double si1;
        double Xi1;
        double Yi1;


        for (int i = 0; i < rawPointList.size(); i++) {

            if(pointsToRemove!=null && pointsToRemove.contains(i))
                continue;

            mi = (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y) / (rawPointList.get((i + 1) % n).x - rawPointList.get(i).x);

            mi1 = (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) / (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x);

            li = Math.sqrt((rawPointList.get((i + 1) % n).x - rawPointList.get(i).x) * (rawPointList.get((i + 1) % n).x - rawPointList.get(i).x) + (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y) * (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y));

            li1 = Math.sqrt((rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x) * (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x) + (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) * (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y));

            ri = rawPointList.get(i).x + offset * (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y) / li;

            ri1 = rawPointList.get((i + 1) % n).x + offset * (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) / li1;

            si = rawPointList.get(i).y - offset * (rawPointList.get((i + 1) % n).x - rawPointList.get(i).x) / li;

            si1 = rawPointList.get((i + 1) % n).y - offset * (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x) / li1;

            Xi1 = (mi1 * ri1 - mi * ri + si - si1) / (mi1 - mi);

            Yi1 = (mi * mi1 * (ri1 - ri) + mi1 * si - mi * si1) / (mi1 - mi);


            if (rawPointList.get((i + 1) % n).x - rawPointList.get((i) % n).x == 0) {
                Xi1 = rawPointList.get((i + 1) % n).x + offset * (rawPointList.get((i + 1) % n).y - rawPointList.get(i % n).y) / Math.abs(rawPointList.get((i + 1) % n).y - rawPointList.get(i % n).y);
                Yi1 = mi1 * Xi1 - mi1 * ri1 + si1;
            }


            if (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x == 0) {

                Xi1 = rawPointList.get((i + 2) % n).x + offset * (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) / Math.abs(rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y);

                Yi1 = mi * Xi1 - mi * ri + si;

            }
            retVal.add(new Vector(Xi1, Yi1));
        }
        return retVal;
    }

    private ArrayList<Integer> findNaNValues(List<Vector> rawPointList) {
        ArrayList<Integer> retVal = new ArrayList<>();

        int n = (int) rawPointList.size();
        double offset = 0;

        double mi;
        double mi1;
        double li;
        double li1;
        double ri;
        double ri1;
        double si;
        double si1;
        double Xi1;
        double Yi1;

        for (int i = 0; i < rawPointList.size(); i++) {
            mi = (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y) / (rawPointList.get((i + 1) % n).x - rawPointList.get(i).x);
            mi1 = (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) / (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x);
            li = Math.sqrt((rawPointList.get((i + 1) % n).x - rawPointList.get(i).x) * (rawPointList.get((i + 1) % n).x - rawPointList.get(i).x) + (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y) * (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y));
            li1 = Math.sqrt((rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x) * (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x) + (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) * (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y));
            ri = rawPointList.get(i).x + offset * (rawPointList.get((i + 1) % n).y - rawPointList.get(i).y) / li;
            ri1 = rawPointList.get((i + 1) % n).x + offset * (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) / li1;
            si = rawPointList.get(i).y - offset * (rawPointList.get((i + 1) % n).x - rawPointList.get(i).x) / li;
            si1 = rawPointList.get((i + 1) % n).y - offset * (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x) / li1;
            Xi1 = (mi1 * ri1 - mi * ri + si - si1) / (mi1 - mi);
            Yi1 = (mi * mi1 * (ri1 - ri) + mi1 * si - mi * si1) / (mi1 - mi);

            if (rawPointList.get((i + 1) % n).x - rawPointList.get((i) % n).x == 0) {
                Xi1 = rawPointList.get((i + 1) % n).x + offset * (rawPointList.get((i + 1) % n).y - rawPointList.get(i % n).y) / Math.abs(rawPointList.get((i + 1) % n).y - rawPointList.get(i % n).y);
                Yi1 = mi1 * Xi1 - mi1 * ri1 + si1;
            }

            if (rawPointList.get((i + 2) % n).x - rawPointList.get((i + 1) % n).x == 0) {
                Xi1 = rawPointList.get((i + 2) % n).x + offset * (rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y) / Math.abs(rawPointList.get((i + 2) % n).y - rawPointList.get((i + 1) % n).y);
                Yi1 = mi * Xi1 - mi * ri + si;
            }
            if (Double.isNaN(Xi1) || Double.isNaN(Yi1))
                retVal.add(i);

        }

        return retVal;
    }

    public ArrayList<Vector> normalizeVectorList(List<Vector> rawPointList) {
        ArrayList<Vector> retVal = new ArrayList<>();

        for (int i = 0; i < rawPointList.size(); i++) {
            Vector a = rawPointList.get(i);
            Vector b = i == rawPointList.size() - 1 ? rawPointList.get(0) : rawPointList.get(i + 1);
            //retVal.add(VectorMath.multiplied(VectorMath.added(a, b), 0.5));
            retVal.add(new Vector((a.x + b.x) / 2, (a.y + b.y) / 2));
        }

        return retVal;
    }


    public void createBunkers() {
        scaledArrays = new ArrayList<>();
        ArrayList<ArrayList<Vector>> vectorArrays = new ArrayList<>();
        ArrayList<Float> baseAltitudes = new ArrayList<>();

        for (double offset = -1.0; offset <= 1.0; offset += 0.2) {
            ArrayList<Vector> polygon = resizePolygon(rawPointList, offset * 0.5 / Constants.LOCATION_SCALE);
            vectorArrays.add(polygon);
        }

        boolean isFirst = true;

        for (int i = vectorArrays.size() - 1; i >= 0; i--) {

            ArrayList<Vector> polygon = new ArrayList<>();

            for (int j = 0; j < vectorArrays.get(i).size(); j++) {

                Vector retVal = vectorArrays.get(i).get(j);

                double z;

                if (isFirst) {
                    float baseAltitude = ElevationHelper.getInstance().getHeightInPoint(retVal, renderer);
                    z = getZPositionForPoint(vectorArrays.size() - 1 - i, retVal, baseAltitude);
                    baseAltitudes.add(baseAltitude);
                } else {
                    z = getZPositionForPoint(vectorArrays.size() - 1 - i, retVal, baseAltitudes.get(j));
                }
                polygon.add(new Vector(retVal.x, retVal.y, z));
            }
            if (isFirst)
                isFirst = false;
            scaledArrays.add(polygon);
        }


    }

}

