package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;
import com.l1inc.viewer.drawing.BaseDrawingObject;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class VectorGPSObject {

    @SerializedName("HoleCount")
    private Integer holeCount;

    @SerializedName("Holes")
    private Holes holes;

    @SerializedName("Water")
    private BaseShapeObject water;

    @SerializedName("Background")
    private BaseShapeObject background;

    @SerializedName("Path")
    private BaseShapeObject path;

    @SerializedName("Sand")
    private BaseShapeObject sand;

    @SerializedName("Lake")
    private BaseShapeObject lake;

    @SerializedName("Lava")
    private BaseShapeObject lava;

    @SerializedName("Ocean")
    private BaseShapeObject ocean;

    @SerializedName("Pond")
    private BaseShapeObject pond;

    @SerializedName("Bridge")
    private BaseShapeObject bridge;

    @SerializedName("Tree")
    private BaseShapeObject Tree;

    @SerializedName("Creek")
    private BaseShapeObject creek;

    public Integer getHoleCount() {
        return holeCount;
    }

    public void setHoleCount(Integer holeCount) {
        this.holeCount = holeCount;
    }

    public Holes getHoles() {
        return holes;
    }

    public void setHoles(Holes holes) {
        this.holes = holes;
    }

    public BaseShapeObject getWater() {
        return water;
    }

    public void setWater(BaseShapeObject water) {
        this.water = water;
    }

    public BaseShapeObject getPath() {
        return path;
    }

    public void setPath(BaseShapeObject path) {
        this.path = path;
    }

    public BaseShapeObject getSand() {
        return sand;
    }

    public void setSand(BaseShapeObject sand) {
        this.sand = sand;
    }

    public BaseShapeObject getLake() {
        return lake;
    }

    public void setLake(BaseShapeObject lake) {
        this.lake = lake;
    }

    public BaseShapeObject getLava() {
        return lava;
    }

    public void setLava(BaseShapeObject lava) {
        this.lava = lava;
    }

    public BaseShapeObject getOcean() {
        return ocean;
    }

    public void setOcean(BaseShapeObject ocean) {
        this.ocean = ocean;
    }

    public BaseShapeObject getPond() {
        return pond;
    }

    public void setPond(BaseShapeObject pond) {
        this.pond = pond;
    }

    public BaseShapeObject getBridge() {
        return bridge;
    }

    public void setBridge(BaseShapeObject bridge) {
        this.bridge = bridge;
    }

    public BaseShapeObject getTree() {
        return Tree;
    }

    public void setTree(BaseShapeObject tree) {
        Tree = tree;
    }

    public BaseShapeObject getCreek() {
        return creek;
    }

    public void setCreek(BaseShapeObject creek) {
        this.creek = creek;
    }

    public BaseShapeObject getBackground() {
        return background;
    }

    public void setBackground(BaseShapeObject background) {
        this.background = background;
    }
}
