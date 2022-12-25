package com.l1inc.viewer.math;

import android.graphics.RectF;
import android.location.Location;

import java.util.List;
import java.util.Locale;

/**
 * Created by Yevhen Paschenko on 3/22/2016.
 */
public class Vector {

    public static String vectorListToDebugString(final List<Vector> vectorList) {
        final StringBuilder stringBuilder = new StringBuilder();

        for (final Vector vector : vectorList) {
            stringBuilder.append(
                    vector.toDebugString()
            );
        }

        return stringBuilder.toString();
    }

    public double x;
    public double y;
    public double z;


    public double normalX;
    public double normalY;
    public double normalZ;

//    public double getX() {
//        return x;
//    }
//
//    public void setX(double x) {
//        this.x = x;
//    }
//
//    public double getY() {
//        return y;
//    }
//
//    public void setY(double y) {
//        this.y = y;
//    }

    public Vector() {
        x = 0;
        y = 0;
        z = 0;
    }

    public void setNormals(double x, double y, double z) {
        normalX = x;
        normalY = y;
        normalZ = z;
    }

    public void revert(){
        this.normalX = -normalX;
        this.normalY = -normalY;
        this.normalZ = -normalZ;
    }

    public void setNormals(Vector retVal) {
        normalX = retVal.x;
        normalY = retVal.y;
        normalZ = retVal.z;
    }

    public Vector(final double x,
                  final double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    public Vector(final double x,
                  final double y,
                  final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector(final Vector vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }

    public Vector(final Location location) {
        x = location.getLongitude();
        y = location.getLatitude();
    }

    public Vector(final float[] P) {
        this.x = P[0];
        this.y = P[1];
    }

    public Vector copy() {
        return new Vector(this);
    }

    public Vector substracted(final Vector v) {
        return VectorMath.subtracted(this, v);
    }

    public Vector added(final Vector v) {
        return VectorMath.added(this, v);
    }

    public Vector normalized() {
        return VectorMath.normalized(this);
    }

    public Vector multiplied(final double factor) {
        return VectorMath.multiplied(this, factor);
    }

    public Vector rotated(final double angle) {
        return VectorMath.rotated(this, angle);
    }

    public double distance(final Vector vector) {
        return VectorMath.distance(this, vector);
    }

    public boolean equals(final Vector other) {
        return x == other.x && y == other.y;
    }

    public Location toLocation() {
        final Location location = new Location("");
        location.setLongitude(x);
        location.setLatitude(y);
        return location;
    }

    public boolean isInsideRectF(final RectF rectF) {
        return rectF.contains((float) x, (float) y);
    }

	/*@Override
    public String toString() {
		return String.format(Locale.US, "%f, %f , %f", x, y,z);
	}*/

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public String toDebugString() {
        return String.format(
                Locale.ENGLISH,
                "_pointList.Add(new PointF(%ff, %ff));\n",
                x, y
        );
    }
}
