package com.l1inc.viewer.elevation;

/**
 * Created by Kirill Kartukov on 04.12.2017.
 */

public class DrawTileHelper {

    private int position;
    private int textureQuality;

    public DrawTileHelper(int position, int textureQuality) {
        this.position = position;
        this.textureQuality = textureQuality;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getTextureQuality() {
        return textureQuality;
    }

    public void setTextureQuality(int textureQuality) {
        this.textureQuality = textureQuality;
    }


    @Override
    public String toString() {
        return "DrawTileHelper{" +
                "position=" + position +
                ", textureQuality=" + textureQuality +
                '}';
    }
}
