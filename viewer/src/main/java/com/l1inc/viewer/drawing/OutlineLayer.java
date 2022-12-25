package com.l1inc.viewer.drawing;

import android.content.Context;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.parcer.BaseShapeObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 5/25/2016.
 */
public class OutlineLayer extends IDrawable {

    private List<OutlinePolygon> outlineList = new ArrayList<>();
    private String layerName;
    private OutlinePolygon polygon;

    private Vector extremeLeft;
    private Vector extremeTop;
    private Vector extremeRight;
    private Vector extremeBottom;

    public OutlineLayer(final String layerName,
                        final BaseShapeObject baseShapeObject,
                        final Context context,
                        final int textureResId,
                        final float width,
                        final Layer.PointInterpolation interpolation,
                        boolean isTextureCompressed) throws Exception {
        this.layerName = layerName + " outline";
        final int shapeCount = baseShapeObject.getShapeCount();
        polygon = null;
        for (int i = 0; i < shapeCount; i++) {
            polygon = new OutlinePolygon(baseShapeObject.getShapes().getShape().get(i), context,
                    textureResId, width, interpolation, isTextureCompressed);
            outlineList.add(polygon);

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
    }

    @Override
    public void draw(Course3DRenderer renderer) {
        for (int i = 0; i < outlineList.size(); i++) {
            outlineList.get(i).draw(renderer);
        }
    }

    public void destroy() {
        for (final OutlinePolygon polygon : outlineList) {
            polygon.destroy();
        }
    }

    @Override
    public String toString() {
        return layerName;
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

    public ArrayList<Vector> getExtremePoints(){
        ArrayList<Vector> extremePoints = new ArrayList<>();
        extremePoints.add(extremeLeft);
        extremePoints.add(extremeRight);
        extremePoints.add(extremeBottom);
        extremePoints.add(extremeTop);
        return extremePoints;
    }
}
