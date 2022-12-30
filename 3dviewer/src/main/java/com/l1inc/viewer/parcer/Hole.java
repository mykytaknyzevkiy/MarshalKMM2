package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class Hole {

    @SerializedName("Greencenter")
    protected BaseShapeObject greenCenter;

    @SerializedName("Perimeter")
    protected BaseShapeObject perimeter;

    @SerializedName("Teebox")
    protected BaseShapeObject teebox;

    @SerializedName("Fairway")
    protected BaseShapeObject fairway;

    @SerializedName("Centralpath")
    protected BaseShapeObject centralpath;

    @SerializedName("Teeboxcenter")
    protected BaseShapeObject teeboxcenter;

    @SerializedName("Green")
    protected BaseShapeObject green;

    @SerializedName("HoleNumber")
    protected Integer holeNumber;

    @SerializedName("Bunker")
    protected BaseShapeObject Bunker;

    public BaseShapeObject getGreenCenter() {
        return greenCenter;
    }

    public void setGreenCenter(BaseShapeObject greenCenter) {
        this.greenCenter = greenCenter;
    }

    public BaseShapeObject getPerimeter() {
        return perimeter;
    }

    public void setPerimeter(BaseShapeObject perimeter) {
        this.perimeter = perimeter;
    }

    public BaseShapeObject getTeebox() {
        return teebox;
    }

    public void setTeebox(BaseShapeObject teebox) {
        this.teebox = teebox;
    }

    public BaseShapeObject getFairway() {
        return fairway;
    }

    public void setFairway(BaseShapeObject fairway) {
        this.fairway = fairway;
    }

    public BaseShapeObject getCentralpath() {
        return centralpath;
    }

    public void setCentralpath(BaseShapeObject centralpath) {
        this.centralpath = centralpath;
    }

    public BaseShapeObject getTeeboxcenter() {
        return teeboxcenter;
    }

    public void setTeeboxcenter(BaseShapeObject teeboxcenter) {
        this.teeboxcenter = teeboxcenter;
    }

    public BaseShapeObject getGreen() {
        return green;
    }

    public void setGreen(BaseShapeObject green) {
        this.green = green;
    }

    public Integer getHoleNumber() {
        return holeNumber;
    }

    public void setHoleNumber(Integer holeNumber) {
        this.holeNumber = holeNumber;
    }

    public BaseShapeObject getBunker() {
        return Bunker;
    }

    public void setBunker(BaseShapeObject bunker) {
        Bunker = bunker;
    }
}
