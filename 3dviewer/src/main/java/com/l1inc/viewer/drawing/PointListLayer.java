package com.l1inc.viewer.drawing;

import android.util.Log;

import com.l1inc.viewer.parcer.BaseShapeObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Yevhen Paschenko on 2/23/2016.
 */
public class PointListLayer {

    private ArrayList<PointList> pointList = new ArrayList<>();

    public ArrayList<PointList> getPointList() {
        return pointList;
    }

    public PointList getFirstPointList() {
        return pointList.get(0);
    }

    public PointListLayer(final BaseShapeObject baseShapeObject,
                          final boolean transform) throws JSONException {
        final int shapeCount = baseShapeObject.getShapeCount();
        for (int i = 0; i < shapeCount; i++) {
            final PointList plist = new PointList(baseShapeObject.getShapes().getShape().get(i), transform);
            pointList.add(plist);
        }
    }

    private void addLog(String mes) {
        Log.e(getClass().getSimpleName(), mes);
    }
}
