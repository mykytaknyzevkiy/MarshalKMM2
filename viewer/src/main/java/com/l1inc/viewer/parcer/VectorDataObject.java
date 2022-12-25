package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;
import com.l1inc.viewer.drawing.BaseDrawingObject;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class VectorDataObject {

    @SerializedName("vectorGPSObject")
    private VectorGPSObject vectorGPSObject;

    public VectorGPSObject getVectorGPSObject() {
        return vectorGPSObject;
    }

    public void setVectorGPSObject(VectorGPSObject vectorGPSObject) {
        this.vectorGPSObject = vectorGPSObject;
    }
}
