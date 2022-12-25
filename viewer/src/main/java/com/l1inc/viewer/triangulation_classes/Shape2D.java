package com.l1inc.viewer.triangulation_classes;

/**
 * Created by Kirill Kartukov on 30.03.2018.
 */

public interface Shape2D {

    /** Returns whether the given point is contained within the shape. */
    boolean contains (Vector2 point);

    /** Returns whether a point with the given coordinates is contained within the shape. */
    boolean contains (float x, float y);

}
