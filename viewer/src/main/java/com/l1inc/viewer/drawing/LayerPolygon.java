package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.ETC1Util;
import android.opengl.GLES20;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.Course3DViewer;
import com.l1inc.viewer.HoleWithinCourse;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Interpolator;
import com.l1inc.viewer.math.PolygonOffsetter;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;
import com.l1inc.viewer.parcer.Shape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yevhen Paschenko on 2/22/2016.
 */
public class LayerPolygon extends BaseDrawingObject {

    private int textureId = -1;
    private RectF boundingBox;
    private Vector extremeLeft;
    private Vector extremeTop;
    private Vector extremeRight;
    private Vector extremeBottom;
    private DrawElement[] drawElements;
    private Geometry geometry;
//    private static Map<String, DrawElement[]> drawElementsMap = new HashMap<>();

    private int textureResId = -1;
    private Context context;
    private boolean isTextureCompressed = false;

    private List<Vector> rawPointList = new ArrayList<>();

    public static native DrawElement[] triangulate(float[] vertices);

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

    public LayerPolygon(final Shape shape,
                        final Context context,
                        final int textureResId,
                        final float extension,
                        final Layer.PointInterpolation interpolation,
                        final boolean isTextureCompressed,
                        final String layerName,
                        final int index) throws Exception {

        this.isTextureCompressed = isTextureCompressed;
        this.context = context;
        this.textureResId = textureResId;

        final String points = shape.getPoints();
        final String[] lonLatPointsArray = points.split(",");

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

        if (extension > 0) {
            rawPointList = PolygonOffsetter.extendPolygon(rawPointList, extension);
        }

        final float[] vertices = new float[rawPointList.size() * 3];
        for (int i = 0; i < rawPointList.size(); i++) {
            final float lon = (float) rawPointList.get(i).x;
            final float lat = (float) rawPointList.get(i).y;
            vertices[i * 3] = lon;
            vertices[i * 3 + 1] = lat;
            vertices[i * 3 + 2] = 0;

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

        drawElements = triangulate(vertices);

//        final String cachedName = layerName + "-" + index;
//        drawElements = drawElementsMap.get(cachedName);
//        if (drawElements == null) {
//            Course3DRenderer.getViewerLogger().info("triangulating layer polygon for: " + cachedName);
//            drawElements = triangulate(vertices);
//            drawElementsMap.put(cachedName, drawElements);
//        }

        Float baseU = null;
        Float baseV = null;
        for (final DrawElement element : drawElements) {
            final ByteBuffer vertexbb = ByteBuffer.allocateDirect(element.getPointList().length * 4);
            vertexbb.order(ByteOrder.nativeOrder());
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
            //element.setPointList(null);
        }

        //loadTexture();

        geometry = toClosedGeometry();
    }

    public void draw(Course3DRenderer renderer) {

        if (textureId == -1 || !TextureCache.hasTexture(textureResId, textureId)) {
            loadTexture();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        for (final DrawElement element : drawElements) {
            if (!Frustum.isPolygonInFrustum(element.getVertexList())) {
                continue;
            }
            if (element.getVboId() != 0) {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, element.getTexId());
                GLES20.glClear(0);
                GLES20.glVertexAttribPointer(renderer.vTexCoord, 2, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, element.getVboId());
                GLES20.glVertexAttribPointer(renderer.aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
                GLES20.glDrawArrays(element.getType(), 0, element.getNumPolygons());
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            } else {
                GLES20.glVertexAttribPointer(renderer.aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, element.getVertexBuffer());
                GLES20.glVertexAttribPointer(renderer.vTexCoord, 2, GLES20.GL_FLOAT, false, 0, element.getTextureCoordinates());
                GLES20.glDrawArrays(element.getType(), 0, element.getNumPolygons());
            }
        }
    }

    private void loadTexture() {
        if (!isTextureCompressed)
            textureId = TextureCache.getTexture(context, textureResId);
        else
            textureId = !ETC1Util.isETC1Supported() ? TextureCache.getTexture(context, textureResId) : TextureCache.getCompressedTexture(context, textureResId);
    }

    public List<Vector> getPointList() {
        return rawPointList == null ? new ArrayList<Vector>() : rawPointList;
    }

    public void destroy() {
        for (final DrawElement element : drawElements) {
            element.destroy();
        }
    }

    public boolean contains(final Vector point) {
        final Geometry geoLocation = new GeometryFactory()
                .createPoint(new Coordinate(point.x, point.y));

        final IntersectionMatrix matrix = geometry.relate(geoLocation);
        return matrix.isContains();
    }

    public Geometry toClosedGeometry() {
        final List<Coordinate> coordinateList = new ArrayList<Coordinate>();
        for (int i = 0; i < rawPointList.size(); i++) {
            coordinateList.add(
                    new Coordinate(
                            rawPointList.get(i).x,
                            rawPointList.get(i).y
                    )
            );
        }

        // make polygon closed if needed
        final Coordinate first = coordinateList.get(0);
        final Coordinate last = coordinateList.get(coordinateList.size() - 1);
        if (first.equals(last) == false) {
            coordinateList.add(coordinateList.get(0));
        }

        return new GeometryFactory().createPolygon(
                coordinateList.toArray(new Coordinate[coordinateList.size()])
        );
    }
}
