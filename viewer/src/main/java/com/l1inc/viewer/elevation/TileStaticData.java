package com.l1inc.viewer.elevation;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kirill Kartukov on 02.12.2017.
 */

public class TileStaticData {

    private static TileStaticData instance;

    public static TileStaticData getInstance() {
        if (null == instance) {
            instance = new TileStaticData();
        }
        return instance;
    }

    private static final int DEFAULT_TQ = 1024;
    public static final int TEXTURE_QUALITY_HIGH = DEFAULT_TQ;
//    public static final int TEXTURE_QUALITY_MEDIUM = DEFAULT_TQ / 2;
//    public static final int TEXTURE_QUALITY_LOW = DEFAULT_TQ / 8;
    public static final int TEXTURE_QUALITY_NONE = 0;
    //Indicies
    private Map<String, ShortBuffer> indiciesMap = new HashMap<>();
    public short[] indices = null;
    public ShortBuffer indexBuffer;
    //UVList
    public List<Float> uvList = new ArrayList<>();
    public FloatBuffer textureBuffer;
    public ByteBuffer byteBuf;
    private Map<Integer, FloatBuffer> textureMap = new HashMap<>();


    public boolean indiciesExist(int height, int width) {
        return indiciesMap.get(height + "" + width) != null;
    }

    public void addIndicies(int height, int width) {
        indiciesMap.put(height + "" + width, indexBuffer);
    }

    public ShortBuffer getIndicies(int height, int width) {
        return indiciesMap.get(height + "" + width);
    }

    public boolean uvListExist(int height, int width) {
        return textureMap.get(height * 5 + width) != null;
    }

    public void addUVList(int height, int width) {
        textureMap.put(height * 5 + width, textureBuffer);
    }

    public FloatBuffer getUVList(int height, int width) {
        return textureMap.get(height * 5 + width);
    }

    public boolean dataExist() {
        return textureMap != null && indiciesMap != null && textureMap.size() > 0 && indiciesMap.size() > 0;
    }

    public void destroy() {
        textureMap.clear();
        indiciesMap.clear();
        instance = null;
    }


}
