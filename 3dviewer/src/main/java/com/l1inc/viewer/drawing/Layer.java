package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.parcer.BaseShapeObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 2/22/2016.
 */
public class Layer extends IDrawable {


    public enum PointInterpolation {
        KeepOriginal,
        Interpolate
    }

    public static Double sBaseLon ;
    public static Double sBaseLat ;

    public static void resetBasePoints() {

        sBaseLon = null;
        sBaseLat = null;

    }

    public static double transformLon(String lonStr) {
        return transformLon(Double.valueOf(lonStr));
    }

    private static void addLog(String mes) {
        Log.e("Layer", mes);
    }

    public static double transformLon(double lon) {
        if (sBaseLon == null) {
            sBaseLon = lon;
        }
        lon = lon - sBaseLon;
        lon = lon * (getMetersInOneLatitudeDegree(sBaseLat) / Constants.LOCATION_SCALE);
        return lon;
    }


    public static double transformToLon(double lon) {
        lon /= (getMetersInOneLatitudeDegree(sBaseLat) / Constants.LOCATION_SCALE);

        lon = lon + sBaseLon;

        return lon;
    }

    public static double transformLat(String latStr) {
        return transformLat(Double.valueOf(latStr));
    }

    public static double transformLat(double lat) {
        if (sBaseLat == null) {
            sBaseLat = lat;
        }

        lat = lat - sBaseLat;
        lat *= (getMetersInOneLongitudeDegree() / Constants.LOCATION_SCALE);
        return lat;
    }

    public static double transformToLat(double lat) {
        if (sBaseLat == null) {
            sBaseLat = lat;
        }

        lat /= (getMetersInOneLongitudeDegree() / Constants.LOCATION_SCALE);
        lat = lat + sBaseLat;

        return lat;
    }

    private static double getMetersInOneLongitudeDegree() {
        return 111111d;
    }

    private static double getMetersInOneLatitudeDegree(double latitude) {
        return 111321.377778 * Math.cos(degreesToRadians(latitude));
    }

    private static double degreesToRadians(double degrees) {
        return (degrees * Math.PI) / 180;
    }

    private RectF boundingBox;
    private Vector extremeLeft;
    private Vector extremeTop;
    private Vector extremeRight;
    private Vector extremeBottom;

    private Vector extremeLeftTop;
    private Vector extremeRightTop;
    private Vector extremeRightBottom;
    private Vector extremeLeftBottom;

    private String layerName;

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

    public Layer(final String layerName,
                 final BaseShapeObject baseShapeObject,
                 final Context context,
                 final int textureResId,
                 final float extension,
                 final PointInterpolation interpolation,
                 final boolean isTextureCompressed) throws Exception {
        this.layerName = layerName;
        this.resID = textureResId;
        setiTextureCompressed(isTextureCompressed);
        int shapeCount = baseShapeObject.getShapeCount();
        layerPolygons = new ArrayList<>();
        for (int i = 0; i < shapeCount; i++) {
            LayerPolygon polygon = new LayerPolygon(baseShapeObject.getShapes().getShape().get(i), context, textureResId, extension, interpolation, isTextureCompressed);
            layerPolygons.add(polygon);

            if (boundingBox == null) {
                boundingBox = new RectF(polygon.getBoundingBox());
            } else {
                boundingBox.union(polygon.getBoundingBox());
            }

            if (extremeLeft == null) {
                extremeLeft = new Vector(polygon.getExtremeLeft());
                extremeRight = new Vector(polygon.getExtremeRight());
                extremeTop = new Vector(polygon.getExtremeTop());
                extremeBottom = new Vector(polygon.getExtremeBottom());
            } else {
                if (extremeLeft.x > polygon.getExtremeLeft().x) {
                    extremeLeft = polygon.getExtremeLeft().copy();
                }
                if (extremeRight.x < polygon.getExtremeRight().x) {
                    extremeRight = polygon.getExtremeRight().copy();
                }
                if (extremeTop.y < polygon.getExtremeTop().y) {
                    extremeTop = polygon.getExtremeTop().copy();
                }
                if (extremeBottom.y > polygon.getExtremeBottom().y) {
                    extremeBottom = polygon.getExtremeBottom().copy();
                }
            }
        }

        extremeLeftTop = new Vector(extremeLeft.x,extremeTop.y);
        extremeLeftBottom = new Vector(extremeLeft.x,extremeBottom.y);
        extremeRightBottom = new Vector(extremeRight.x,extremeBottom.y);
        extremeRightTop = new Vector(extremeRight.x,extremeTop.y);
    }

    @Override
    public void draw(Course3DRenderer renderer) {
        for (int i = 0; i < layerPolygons.size(); i++) {
            layerPolygons.get(i).draw(renderer);
        }
    }

    public void destroy() {
        for (LayerPolygon polygon : layerPolygons) {
            polygon.destroy();
        }
    }

    @Override
    public String toString() {
        return layerName;
    }

    public List<Vector> getExtremeBox(){
        List<Vector> retval = new ArrayList<>();
        retval.add(extremeLeftBottom);
        retval.add(extremeLeftTop);
        retval.add(extremeRightTop);
        retval.add(extremeRightBottom);
        return retval;
    }
}
