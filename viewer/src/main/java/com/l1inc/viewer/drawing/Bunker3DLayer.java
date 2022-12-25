package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.parcer.BaseShapeObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kirill Kartukov on 05.04.2018.
 */

//public class Bunker3DLayer extends IDrawable {
//
//
//    public enum PointInterpolation {
//        KeepOriginal,
//        Interpolate
//    }
//
//    public static Double sBaseLon;
//    public static Double sBaseLat;
//
//    public static void resetBasePoints() {
//
//        sBaseLon = null;
//        sBaseLat = null;
//
//    }
//
//    public static double transformLon(String lonStr) {
//        return transformLon(Double.valueOf(lonStr));
//    }
//
//    private static void addLog(String mes) {
//        Log.e("Layer", mes);
//    }
//
//    public static double transformLon(double lon) {
//        if (sBaseLon == null) {
//            sBaseLon = lon;
//        }
//        lon = lon - sBaseLon;
//        lon = lon * (getMetersInOneLatitudeDegree(sBaseLat) / Constants.LOCATION_SCALE);
//        return lon;
//    }
//
//
//    public static double transformToLon(double lon) {
//        lon /= (getMetersInOneLatitudeDegree(sBaseLat) / Constants.LOCATION_SCALE);
//
//        lon = lon + sBaseLon;
//
//        return lon;
//    }
//
//    public static double transformLat(String latStr) {
//        return transformLat(Double.valueOf(latStr));
//    }
//
//    public static double transformLat(double lat) {
//        if (sBaseLat == null) {
//            sBaseLat = lat;
//        }
//
//        lat = lat - sBaseLat;
//        lat *= (getMetersInOneLongitudeDegree() / Constants.LOCATION_SCALE);
//        return lat;
//    }
//
//    public static double transformToLat(double lat) {
//        if (sBaseLat == null) {
//            sBaseLat = lat;
//        }
//
//        lat /= (getMetersInOneLongitudeDegree() / Constants.LOCATION_SCALE);
//        lat = lat + sBaseLat;
//
//        return lat;
//    }
//
//    private static double getMetersInOneLongitudeDegree() {
//        return 111111d;
//    }
//
//    private static double getMetersInOneLatitudeDegree(double latitude) {
//        return 111321.377778 * Math.cos(degreesToRadians(latitude));
//    }
//
//    private static double degreesToRadians(double degrees) {
//        return (degrees * Math.PI) / 180;
//    }
//
//    private RectF boundingBox;
//    private Vector extremeLeft;
//    private Vector extremeTop;
//    private Vector extremeRight;
//    private Vector extremeBottom;
//    private String layerName;
//
//    public RectF getBoundingBox() {
//        return boundingBox;
//    }
//
//    public Vector getExtremeLeft() {
//        return extremeLeft;
//    }
//
//    public void setExtremeLeft(Vector extremeLeft) {
//        this.extremeLeft = extremeLeft;
//    }
//
//    public Vector getExtremeTop() {
//        return extremeTop;
//    }
//
//    public void setExtremeTop(Vector extremeTop) {
//        this.extremeTop = extremeTop;
//    }
//
//    public Vector getExtremeRight() {
//        return extremeRight;
//    }
//
//    public void setExtremeRight(Vector extremeRight) {
//        this.extremeRight = extremeRight;
//    }
//
//    public Vector getExtremeBottom() {
//        return extremeBottom;
//    }
//
//    public void setExtremeBottom(Vector extremeBottom) {
//        this.extremeBottom = extremeBottom;
//    }
//
//    public Bunker3DLayer(final String layerName,
//                         final BaseShapeObject baseShapeObject,
//                         final Context context,
//                         final int textureResId,
//                         final float extension,
//                         final Layer.PointInterpolation interpolation,
//                         final boolean isTextureCompressed, Course3DRenderer renderer) throws Exception {
//        this.layerName = layerName;
//        this.resID = textureResId;
//        setiTextureCompressed(isTextureCompressed);
//        int shapeCount = baseShapeObject.getShapeCount();
//        bunker3DPolygons = new ArrayList<>();
//        for (int i = 0; i < shapeCount; i++) {
//            Bunker3DPolygon polygon = new Bunker3DPolygon(layerName, baseShapeObject.getShapes().getShape().get(i), context, textureResId, extension, interpolation, isTextureCompressed, this, renderer);
//            bunker3DPolygons.add(polygon);
//
//            if (boundingBox == null) {
//                boundingBox = new RectF(polygon.getBoundingBox());
//            } else {
//                boundingBox.union(polygon.getBoundingBox());
//            }
//
//            if (extremeLeft == null) {
//                extremeLeft = new Vector(polygon.getExtremeLeft());
//                extremeRight = new Vector(polygon.getExtremeRight());
//                extremeTop = new Vector(polygon.getExtremeTop());
//                extremeBottom = new Vector(polygon.getExtremeBottom());
//            } else {
//                if (extremeLeft.x > polygon.getExtremeLeft().x) {
//                    extremeLeft = polygon.getExtremeLeft().copy();
//                }
//                if (extremeRight.x < polygon.getExtremeRight().x) {
//                    extremeRight = polygon.getExtremeRight().copy();
//                }
//                if (extremeTop.y < polygon.getExtremeTop().y) {
//                    extremeTop = polygon.getExtremeTop().copy();
//                }
//                if (extremeBottom.y > polygon.getExtremeBottom().y) {
//                    extremeBottom = polygon.getExtremeBottom().copy();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void draw(Course3DRenderer renderer) {
//        for (int i = 0; i < bunker3DPolygons.size(); i++) {
//            bunker3DPolygons.get(i).draw(renderer);
//        }
//    }
//
//    public void destroy() {
//        for (Bunker3DPolygon polygon : bunker3DPolygons) {
//            polygon.destroy();
//        }
//    }
//
//    @Override
//    public String toString() {
//        return layerName;
//    }
//
//
//
//
//}


