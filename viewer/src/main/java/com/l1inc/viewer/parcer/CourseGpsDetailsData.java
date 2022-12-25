package com.l1inc.viewer.parcer;

import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kirill Kartukov on 03.04.2018.
 */
@Keep
public class CourseGpsDetailsData implements Serializable {

    @SerializedName("holeNumber")
    private Integer holeNumber;

    @SerializedName("frontLat")
    private Double frontLat;

    @SerializedName("frontLon")
    private Double frontLon;

    @SerializedName("centerLat")
    private Double centerLat;

    @SerializedName("centerLon")
    private Double centerLon;

    @SerializedName("backLat")
    private Double backLat;

    @SerializedName("backLon")
    private Double backLon;

    @SerializedName("teeLat1")
    private Double teeLat1;

    @SerializedName("teeLon1")
    private Double teeLon1;

    @SerializedName("teeLat2")
    private Double teeLat2;

    @SerializedName("teeLon2")
    private Double teeLon2;

    @SerializedName("teeLat3")
    private Double teeLat3;

    @SerializedName("teeLon3")
    private Double teeLon3;

    @SerializedName("teeLat4")
    private Double teeLat4;

    @SerializedName("teeLon4")
    private Double teeLon4;

    @SerializedName("teeLat5")
    private Double teeLat5;

    @SerializedName("teeLon5")
    private Double teeLon5;

    private Double customLat1;
    private Double customLon1;
    private Double customLat2;
    private Double customLon2;
    private Double customLat3;
    private Double customLon3;
    private Double customLat4;
    private Double customLon4;

    public Integer getHoleNumber() {
        return holeNumber;
    }

    public void setHoleNumber(Integer holeNumber) {
        this.holeNumber = holeNumber;
    }

    public Double getFrontLat() {
        return frontLat;
    }

    public void setFrontLat(Double frontLat) {
        this.frontLat = frontLat;
    }

    public Double getFrontLon() {
        return frontLon;
    }

    public void setFrontLon(Double frontLon) {
        this.frontLon = frontLon;
    }

    public Double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(Double centerLat) {
        this.centerLat = centerLat;
    }

    public Double getCenterLon() {
        return centerLon;
    }

    public void setCenterLon(Double centerLon) {
        this.centerLon = centerLon;
    }

    public Double getBackLat() {
        return backLat;
    }

    public void setBackLat(Double backLat) {
        this.backLat = backLat;
    }

    public Double getBackLon() {
        return backLon;
    }

    public void setBackLon(Double backLon) {
        this.backLon = backLon;
    }

    public Double getTeeLat1() {
        return teeLat1;
    }

    public void setTeeLat1(Double teeLat1) {
        this.teeLat1 = teeLat1;
    }

    public Double getTeeLon1() {
        return teeLon1;
    }

    public void setTeeLon1(Double teeLon1) {
        this.teeLon1 = teeLon1;
    }

    public Double getTeeLat2() {
        return teeLat2;
    }

    public void setTeeLat2(Double teeLat2) {
        this.teeLat2 = teeLat2;
    }

    public Double getTeeLon2() {
        return teeLon2;
    }

    public void setTeeLon2(Double teeLon2) {
        this.teeLon2 = teeLon2;
    }

    public Double getTeeLat3() {
        return teeLat3;
    }

    public void setTeeLat3(Double teeLat3) {
        this.teeLat3 = teeLat3;
    }

    public Double getTeeLon3() {
        return teeLon3;
    }

    public void setTeeLon3(Double teeLon3) {
        this.teeLon3 = teeLon3;
    }

    public Double getTeeLat4() {
        return teeLat4;
    }

    public void setTeeLat4(Double teeLat4) {
        this.teeLat4 = teeLat4;
    }

    public Double getTeeLon4() {
        return teeLon4;
    }

    public void setTeeLon4(Double teeLon4) {
        this.teeLon4 = teeLon4;
    }

    public Double getTeeLat5() {
        return teeLat5;
    }

    public void setTeeLat5(Double teeLat5) {
        this.teeLat5 = teeLat5;
    }

    public Double getTeeLon5() {
        return teeLon5;
    }

    public void setTeeLon5(Double teeLon5) {
        this.teeLon5 = teeLon5;
    }

    public Double getCustomLat1() {
        return customLat1;
    }

    public void setCustomLat1(Double customLat1) {
        this.customLat1 = customLat1;
    }

    public Double getCustomLon1() {
        return customLon1;
    }

    public void setCustomLon1(Double customLon1) {
        this.customLon1 = customLon1;
    }

    public Double getCustomLat2() {
        return customLat2;
    }

    public void setCustomLat2(Double customLat2) {
        this.customLat2 = customLat2;
    }

    public Double getCustomLon2() {
        return customLon2;
    }

    public void setCustomLon2(Double customLon2) {
        this.customLon2 = customLon2;
    }

    public Double getCustomLat3() {
        return customLat3;
    }

    public void setCustomLat3(Double customLat3) {
        this.customLat3 = customLat3;
    }

    public Double getCustomLon3() {
        return customLon3;
    }

    public void setCustomLon3(Double customLon3) {
        this.customLon3 = customLon3;
    }

    public Double getCustomLat4() {
        return customLat4;
    }

    public void setCustomLat4(Double customLat4) {
        this.customLat4 = customLat4;
    }

    public Double getCustomLon4() {
        return customLon4;
    }

    public void setCustomLon4(Double customLon4) {
        this.customLon4 = customLon4;
    }

    public Location getFrontLocation() {
        final Location location = new Location("");
        location.setLongitude(getFrontLon());
        location.setLatitude(getFrontLat());
        return location;
    }

    public Location getBackLocation() {
        final Location location = new Location("");
        location.setLongitude(getBackLon());
        location.setLatitude(getBackLat());
        return location;
    }

    public List<Location> getCustomPoints() {
        final List<Location> retval = new ArrayList<Location>();

        try {
            for (int i = 1; i <= 4; i++) {
                final Field latField = CourseGpsDetailsData.class.getDeclaredField("customLat" + i);
                final Field lonField = CourseGpsDetailsData.class.getDeclaredField("customLon" + i);
                final Double lat = (Double) latField.get(this);
                final Double lon = (Double) lonField.get(this);

                if (lat == null || lon == null)
                    continue;
                if (lat == 0 && lon == 0)
                    continue;
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(lat);
                location.setLongitude(lon);
                retval.add(location);
            }
        } catch (NoSuchFieldException e) {

        } catch (IllegalAccessException e) {

        }

        return retval;
    }
}
