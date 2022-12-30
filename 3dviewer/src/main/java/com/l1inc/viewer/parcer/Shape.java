package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class Shape {

    @SerializedName("Points")
    private String points;

    @SerializedName("Attributes")
    private Attributes attributes;

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return points;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }
}
