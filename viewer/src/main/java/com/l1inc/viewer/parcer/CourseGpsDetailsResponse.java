package com.l1inc.viewer.parcer;

import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kirill Kartukov on 03.04.2018.
 */
@Keep
public class CourseGpsDetailsResponse implements Serializable {

    @SerializedName("clubLat")
    public Double clubLat;

    @SerializedName("clubLon")
    public Double clubLon;

    @SerializedName("layoutHoles")
    public Integer layoutHoles;

    @SerializedName("GPSList")
    private List<CourseGpsDetailsData> GPSList;

    public List<CourseGpsDetailsData> getGPSList() {
        return GPSList;
    }

    public void setGPSList(List<CourseGpsDetailsData> GPSList) {
        this.GPSList = GPSList;
    }

}
