package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class ElevationDataObject {


    @SerializedName("step")
    private Float mElevationStep;

    @SerializedName("maxLatitude")
    private Double mElevationMaxLatitude;

    @SerializedName("minLongitude")
    private Double mElevationMinLongitude;

    @SerializedName("elevationArray")
    private ArrayList<ArrayList<Float>> mElevationArray;

    public Float getmElevationStep() {
        return mElevationStep;
    }

    public void setmElevationStep(Float mElevationStep) {
        this.mElevationStep = mElevationStep;
    }

    public Double getmElevationMaxLatitude() {
        return mElevationMaxLatitude;
    }

    public void setmElevationMaxLatitude(Double mElevationMaxLatitude) {
        this.mElevationMaxLatitude = mElevationMaxLatitude;
    }

    public Double getmElevationMinLongitude() {
        return mElevationMinLongitude;
    }

    public void setmElevationMinLongitude(Double mElevationMinLongitude) {
        this.mElevationMinLongitude = mElevationMinLongitude;
    }

    public ArrayList<ArrayList<Float>> getmElevationArray() {
        return mElevationArray;
    }

    public void setmElevationArray(ArrayList<ArrayList<Float>> mElevationArray) {
        this.mElevationArray = mElevationArray;
    }
}
