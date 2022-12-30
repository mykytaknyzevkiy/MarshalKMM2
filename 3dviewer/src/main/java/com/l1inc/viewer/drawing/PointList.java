package com.l1inc.viewer.drawing;

import android.graphics.RectF;

import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.parcer.Shape;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 2/23/2016.
 */
public class PointList {

    private Vector[] pointList;
    private RectF boundingBox;

    public Vector[] getPointList() {
        return pointList;
    }

    public final Vector getFirstPoint() {
        return new Vector(pointList[0]);
    }

    public final Vector getLastPoint() {
        return new Vector(pointList[pointList.length - 1]);
    }

    public void setLastPoint(Vector point) {
        if (point != null)
            pointList[pointList.length - 1] = point;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public PointList(final Shape shape,
                     final boolean transform) throws JSONException {

        final String points = shape.getPoints();
        final String[] lonLatPointsArray = points.split(",");
        final List<Vector> vectorList = new ArrayList<>();

        for (int i = 0; i < lonLatPointsArray.length; i++) {
            String lonLatPoints = lonLatPointsArray[i];
            String[] lonLatPair = lonLatPoints.split(" ");
            float lat = Float.valueOf(lonLatPair[1]);
            float lon = Float.valueOf(lonLatPair[0]);
            if (transform) {
                lat = (float) Layer.transformLat(lat);
                lon = (float) Layer.transformLon(lon);
                ;
            }
            final Vector newVector = new Vector(lon, lat);
            if (vectorList.size() > 0) {
                Vector prev = vectorList.get(vectorList.size() - 1);
                if (prev.equals(newVector)) {
                    continue;
                }
            }

            vectorList.add(newVector);

            if (boundingBox == null) {
                boundingBox = new RectF(lon, lat, lon, lat);
            } else {
                boundingBox.union(lon, lat);
            }
        }

        pointList = vectorList.toArray(new Vector[vectorList.size()]);
    }

}
