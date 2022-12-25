package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class BaseShapeObject {

    @SerializedName("ShapeCount")
    private Integer shapeCount;

    @SerializedName("Shapes")
    private Shapes shapes;

    public Integer getShapeCount() {
        return shapeCount;
    }

    public void setShapeCount(Integer shapeCount) {
        this.shapeCount = shapeCount;
    }

    public Shapes getShapes() {
        return shapes;
    }

    public void setShapes(Shapes shapes) {
        this.shapes = shapes;
    }

    @Override
    public String toString() {
        return "BaseShapeObject{" +
                "shapeCount=" + shapeCount +
                ", shapes=" + shapes +
                '}';
    }
}
