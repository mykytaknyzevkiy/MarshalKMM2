package com.l1inc.viewer.math;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 3/22/2016.
 */
public class VectorMath {

    public enum VectorOrder {
        CW,
        CCW
    }

    public static float calculateZPositionForPoint(double x, double y, Vector A, Vector B, Vector C) {

        double p1 = A.x * B.y * C.z;
        double p2 = A.x * B.z * C.y;
        double p3 = A.x * B.z * y;
        double p4 = A.x * C.z * y;
        double p5 = A.y * B.x * C.z;
        double p6 = A.y * B.z * C.x;
        double p7 = A.y * B.z * x;
        double p8 = A.y * C.z * x;
        double p9 = A.z * B.x * C.y;
        double p10 = A.z * B.x * y;
        double p11 = A.z * B.y * C.x;
        double p12 = A.z * B.y * x;
        double p13 = A.z * C.x * y;
        double p14 = A.z * C.y * x;
        double p15 = B.x * C.z * y;
        double p16 = B.y * C.z * x;
        double p17 = B.z * C.x * y;
        double p18 = B.z * C.y * x;

        double d1 = A.x * B.y;
        double d2 = A.x * C.y;
        double d3 = A.y * B.x;
        double d4 = A.y * C.x;
        double d5 = B.x * C.y;
        double d6 = B.y * C.x;

        double p = p1 - p2 + p3 - p4 - p5 + p6 - p7 + p8 + p9 - p10 - p11 + p12 + p13 - p14 + p15 - p16 - p17 + p18;
        double d = d1 - d2 - d3 + d4 + d5 - d6;

        return (float) (p / d);
    }

    public static double angle(final Vector pt1,
                               final Vector pt2,
                               final Vector pt3) {
//		final Vector v1 = new Vector(pt1.x - pt2.x, pt1.y - pt2.y);
//		final Vector v2 = new Vector(pt3.x - pt2.x, pt3.y - pt2.y);
//		double cos = (v1.x * v2.x + v1.y * v2.y) / (Math.sqrt(v1.x * v1.x + v1.y * v1.y) * Math.sqrt(v2.x * v2.x + v2.y * v2.y));
//		cos = Math.max(cos, -1);
//		cos = Math.min(cos, 1);
//		return Math.acos(cos);
        return angle(pt1.x, pt1.y, pt2.x, pt2.y, pt3.x, pt3.y);
    }

    public static double angle(final double pt1x,
                               final double pt1y,
                               final double pt2x,
                               final double pt2y,
                               final double pt3x,
                               final double pt3y) {
        final double v1x = pt1x - pt2x;
        final double v1y = pt1y - pt2y;
        final double v2x = pt3x - pt2x;
        final double v2y = pt3y - pt2y;
        //addLog(v1x);
        //addLog(v1y);
        //addLog(v2x);
        //addLog(v2y);
        double cos = (v1x * v2x + v1y * v2y) / (Math.sqrt(v1x * v1x + v1y * v1y) * Math.sqrt(v2x * v2x + v2y * v2y));
        cos = Math.max(cos, -1);
        cos = Math.min(cos, 1);
        return Math.acos(cos);
    }


    protected static void addLog(Double mes) {
        Log.e("VectorMath", String.valueOf(mes));
    }

    protected static void addLog(String mes) {
        Log.e("VectorMath", String.valueOf(mes));
    }

    public static void normalize(final Vector ptOut,
                                 final Vector pt) {
        final double inv_len = 1.0 / distance(pt);
        ptOut.x = pt.x * inv_len;
        ptOut.y = pt.y * inv_len;
    }

    public static Vector normalized(final Vector pt) {
        final double inv_len = 1.0 / distance(pt);
        return new Vector(pt.x * inv_len, pt.y * inv_len, pt.z * inv_len);
    }

    public static Vector normalized2(final Vector pt) {
        final double inv_len = distance(pt);
        return new Vector(pt.x / inv_len, pt.y / inv_len, pt.z / inv_len);
    }

    public static double distance(final Vector pt1,
                                  final Vector pt2) {
        return Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
    }

    public static double distanceWithVector1(final Vector pt1,
                                             final Vector pt2) {
        return Math.sqrt(Math.pow(pt1.x - pt2.x, 2) + Math.pow(pt1.y - pt2.y, 2));
    }

    public static double distance(final Vector vector) {
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y + vector.z * vector.z);
    }

    public static double lengthSquared(final Vector pt1,
                                       final Vector pt2) {
        return Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2);
    }

    private static Vector rotatedVector = new Vector();

    public static Vector rotated(final Vector pt,
                                 final double angle) {
        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);
        final double newX = -(pt.y) * sin + (pt.x) * cos;
        final double newY = (pt.y) * cos + (pt.x) * sin;
        if (rotatedVector == null)
            rotatedVector = new Vector();
        rotatedVector.x = newX;
        rotatedVector.y = newY;
        return rotatedVector;
    }

    public static Vector subtracted(final Vector pt1,
                                    final Vector pt2) {
        return new Vector(pt1.x - pt2.x, pt1.y - pt2.y, pt1.z - pt2.z);
    }

    private static Vector mV1;
    private static Vector mV2;
    private static Vector normal;

    public static Vector getNormalForTriangle(Vector p1, Vector p2, Vector p3) {
        mV1 = subtracted(p2, p1);
        mV2 = subtracted(p3, p1);
        normal = cross(mV1, mV2);
        normal = normalized(normal);
        return normal;
    }

    private static Vector v1 = new Vector();
    private static Vector v2 = new Vector();
    private static Vector n = new Vector();

    public static Vector getNormals2(Vector a, Vector b, Vector c) {

        v1.x = a.x - b.x;
        v1.y = a.y - b.y;
        v1.z = a.z - b.z;

        v2.x = b.x - c.x;
        v2.y = b.y - c.y;
        v2.z = b.z - c.z;

        double wrki = Math.sqrt((v1.y * v2.z - v1.z * v2.y) * (v1.y * v2.z - v1.z * v2.y) + (v1.z * v2.x - v1.x * v2.z) * (v1.z * v2.x - v1.x * v2.z) + (v1.x * v2.y - v1.y * v2.x) * (v1.x * v2.y - v1.y * v2.x));
        if (wrki != 0) {
            n.x = (v1.y * v2.z - v1.z * v2.y) / wrki;
            n.y = (v1.z * v2.x - v1.x * v2.z) / wrki;
            n.z = (v1.x * v2.y - v1.y * v2.x) / wrki;
        } else {
            n.x = 0;
            n.y = 0;
            n.z = 0;
        }

        return n;

    }


    public static Vector cross(Vector left, Vector right) {
        Vector vector = new Vector();

        vector.x = left.y * right.z - left.z * right.y;
        vector.y = left.z * right.x - left.x * right.z;
        vector.z = left.x * right.y - left.y * right.x;

        return vector;
    }

    public static void substract(final Vector ptOut,
                                 final Vector pt1,
                                 final Vector pt2) {
        ptOut.x = pt1.x - pt2.x;
        ptOut.y = pt1.y - pt2.y;
    }

    public static Vector added(final Vector pt1,
                               final Vector pt2) {
        return new Vector(pt1.x + pt2.x, pt1.y + pt2.y);
    }

    public static void add(final Vector ptOut,
                           final Vector pt1,
                           final Vector pt2) {
        ptOut.x = pt1.x + pt2.x;
        ptOut.y = pt1.y + pt2.y;
    }

    public static Vector multiplied(final Vector pt1,
                                    final double factor) {
        return new Vector(pt1.x * factor, pt1.y * factor);
    }

    public static void multiply(final Vector ptOut,
                                final Vector pt1,
                                final double factor) {
        ptOut.x = pt1.x * factor;
        ptOut.y = pt1.y * factor;
    }

    public static double deg2rad(final double deg) {
        return deg * Math.PI / 180;
    }

    public static double rad2deg(final double rad) {
        return rad / Math.PI * 180;
    }

    public static double distance(final Vector[] vectors) {
        double retval = 0;

        if (vectors.length > 1) {
            for (int i = 0; i < vectors.length - 1; i++) {
                retval += vectors[i].distance(vectors[i + 1]);
            }
        }

        return retval;
    }

    public static double dotProduct(final Vector pt1,
                                    final Vector pt2,
                                    final Vector pt3) {
        final Vector v1 = new Vector(pt1.x - pt2.x, pt1.y - pt2.y);
        final Vector v2 = new Vector(pt3.x - pt2.x, pt3.y - pt2.y);

        return v1.x * v2.x + v1.y * v2.y;
    }

    public static VectorOrder getVectorOrder(final List<Vector> vectorList) {
        double sum = 0.0;
        for (int i = 0; i < vectorList.size(); i++) {
            final Vector v1 = vectorList.get(i);
            final Vector v2 = vectorList.get((i + 1) % vectorList.size());
            sum += (v2.x - v1.x) * (v2.y + v1.y);
        }

        return sum > 0.0 ? VectorOrder.CW : VectorOrder.CCW;
    }

    public static Vector calculateProjectionPoint(final Vector point,
                                                  final Vector linePoint1,
                                                  final Vector linePoint2) {
        Vector retval = null;
        final double lengthSquared = VectorMath.lengthSquared(linePoint1, linePoint2);
        final Vector p = new Vector(point);

        if (lengthSquared == 0)
            return linePoint1;

        double projectionFactor = VectorMath.dotProduct(p, linePoint1, linePoint2) / lengthSquared;
        if (projectionFactor >= 0 && projectionFactor <= 1) {
            retval = linePoint2.substracted(linePoint1).multiplied(projectionFactor).added(linePoint1);
        }

        return retval;
    }

    public static Vector centeroid(final Vector... vectors) {
        Vector retval = new Vector(0, 0);
        for (final Vector vector : vectors) {
            retval = retval.added(vector);
        }
        retval = retval.multiplied(1.0 / vectors.length);
        return retval;
    }

    public static boolean isVectorInsidePolygon(Vector vector, ArrayList<Vector> polygon) {
        boolean c = false;
        if (vector == null)
            return c;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            if (polygon.get(i) == null || polygon.get(j) == null)
                continue;
            if (((polygon.get(i).y > vector.y) != (polygon.get(j).y > vector.y)) &&
                    (vector.x < (polygon.get(j).x - polygon.get(i).x) * (vector.y - polygon.get(i).y) / (polygon.get(j).y - polygon.get(i).y) + polygon.get(i).x)) {
                c = !c;
            }
        }

        return c;
    }

    public static Vector intersectionWithVector(Vector p1, Vector p2, Vector p3, Vector p4) {
        if (p1 == null || p2 == null || p3 == null || p4 == null)
            return null;
        double d = (p2.x - p1.x) * (p4.y - p3.y) - (p2.y - p1.y) * (p4.x - p3.x);

        if (d == 0)
            return null;

        double u = ((p3.x - p1.x) * (p4.y - p3.y) - (p3.y - p1.y) * (p4.x - p3.x)) / d;
        double v = ((p3.x - p1.x) * (p2.y - p1.y) - (p3.y - p1.y) * (p2.x - p1.x)) / d;

        if (u < 0.0 || u > 1.0)
            return null;
        if (v < 0.0 || v > 1.0)
            return null;

        Vector intersection = new Vector();
        intersection.x = p1.x + u * (p2.x - p1.x);
        intersection.y = p1.y + u * (p2.y - p1.y);

        return intersection;
    }
}
