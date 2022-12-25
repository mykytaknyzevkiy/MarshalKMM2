package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class Shapes {

    @SerializedName("Shape")
    private ArrayList<Shape> shape;

    public ArrayList<Shape> getShape() {
        return shape;
    }

    public void setShape(ArrayList<Shape> shape) {
        this.shape = shape;
    }

    @Override
    public String toString() {
        return "Shapes{" +
                "shape=" + shape +
                '}';
    }
}
