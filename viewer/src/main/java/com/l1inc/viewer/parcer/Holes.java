package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class Holes {

    @SerializedName("Hole")
    private ArrayList<Hole> holes;

    public ArrayList<Hole> getHoles() {
        return holes;
    }

    public void setHoles(ArrayList<Hole> holes) {
        this.holes = holes;
    }
}
