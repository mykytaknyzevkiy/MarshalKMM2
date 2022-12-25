package com.l1inc.viewer;

import com.l1inc.viewer.math.Vector;

/**
 * Created by Kirill Kartukov on 18.01.2018.
 */

public class FlyoverParameters {

    private double defaultZoom;
    private double startViewShift;
    private double endViewShift;
    private double endZoom;
    private double zoomSpeed;
    private double holeAltitude;
    Vector startPos;
    Vector endPos;

    public FlyoverParameters(){
    }

    public FlyoverParameters(double defaultZoom, double startViewShift, double endViewShift, double endZoom, double zoomSpeed, double holeAltitude, Vector startPos, Vector endPos) {
        this.defaultZoom = defaultZoom;
        this.startViewShift = startViewShift;
        this.endViewShift = endViewShift;
        this.endZoom = endZoom;
        this.zoomSpeed = zoomSpeed;
        this.holeAltitude = holeAltitude;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public double getDefaultZoom() {
        return defaultZoom;
    }

    public void setDefaultZoom(double defaultZoom) {
        this.defaultZoom = defaultZoom;
    }

    public double getStartViewShift() {
        return startViewShift;
    }

    public void setStartViewShift(double startViewShift) {
        this.startViewShift = startViewShift;
    }

    public double getEndViewShift() {
        return endViewShift;
    }

    public void setEndViewShift(double endViewShift) {
        this.endViewShift = endViewShift;
    }

    public double getEndZoom() {
        return endZoom;
    }

    public void setEndZoom(double endZoom) {
        this.endZoom = endZoom;
    }

    public double getZoomSpeed() {
        return zoomSpeed;
    }

    public void setZoomSpeed(double zoomSpeed) {
        this.zoomSpeed = zoomSpeed;
    }

    public double getHoleAltitude() {
        return holeAltitude;
    }

    public void setHoleAltitude(double holeAltitude) {
        this.holeAltitude = holeAltitude;
    }

    public Vector getStartPos() {
        return startPos;
    }

    public void setStartPos(Vector startPos) {
        this.startPos = startPos;
    }

    public Vector getEndPos() {
        return endPos;
    }

    public void setEndPos(Vector endPos) {
        this.endPos = endPos;
    }

    @Override
    public String toString() {
        return "FlyoverParameters{" +
                "defaultZoom=" + defaultZoom +
                ", startViewShift=" + startViewShift +
                ", endViewShift=" + endViewShift +
                ", endZoom=" + endZoom +
                ", zoomSpeed=" + zoomSpeed +
                ", holeAltitude=" + holeAltitude +
                ", startPos=" + startPos +
                ", endPos=" + endPos +
                '}';
    }
}
