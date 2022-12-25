package com.l1inc.viewer.drawing;

import com.l1inc.viewer.Course3DRenderer;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Yevhen Paschenko on 3/30/2017.
 */

public class IDrawable {

    private String name;

    protected int resID;

    protected int backGroundTextureID;

    protected ArrayList<LayerPolygon> layerPolygons;

//    protected ArrayList<Bunker3DPolygon> bunker3DPolygons;

    public boolean iTextureCompressed;

    public void draw(Course3DRenderer renderer) {
    }

    public void destroy() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getResID() {
        return resID;
    }

    public ArrayList<LayerPolygon> getLayerPolygons() {
        return layerPolygons;
    }

//    public ArrayList<Bunker3DPolygon> getBunkerPolygons() {
//        return bunker3DPolygons;
//    }

    public boolean isiTextureCompressed() {
        return iTextureCompressed;
    }

    public void setiTextureCompressed(boolean iTextureCompressed) {
        this.iTextureCompressed = iTextureCompressed;
    }

    public int getBackGroundTextureID() {
        return backGroundTextureID;
    }

    public void setBackGroundTextureID(int backGroundTextureID) {
        this.backGroundTextureID = backGroundTextureID;
    }
}
