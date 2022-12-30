package com.l1inc.viewer.parcer;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Kirill Kartukov on 07.12.2017.
 */

public class Attributes {

    @SerializedName("Type")
    private Integer type;

    @SerializedName("Size")
    private Integer size;

    @SerializedName("Description")
    private Integer description;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getDescription() {
        return description;
    }

    public void setDescription(Integer description) {
        this.description = description;
    }
}
