package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.Keep;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.Course3DRendererBase;
import com.l1inc.viewer.R;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Kirill Kartukov on 01.11.2017.
 */

public class Callouts extends BaseDrawingObject {

    private final float CALLOUT_SCALE = 0.045f;
    private final float LOCATION_SCALE = 0.005f;
    private final float CURSOR_SCALE = 0.015f;

    @Keep
    public enum CalloutsDrawMode {
        TWO_SEGMENTS,
        ONE_SEGMENT
    }

    private Location currentLocation;
    private CalloutsDrawMode calloutsDrawMode;
    private PointListLayer centralPath;
    private boolean showOverlay;

    private float[] twoLineB = new float[9];
    private float[] oneLineB = new float[6];

    private boolean hasFocus = false;

    private IndexedTexturedSquare locationPolygon;
    private IndexedTexturedSquare cursorPolygon;

    private int color100;
    private int color150;
    private int color200;
    private int color250;
    private int color300;

    private IndexedTexturedSquare distance100;
    private IndexedTexturedSquare distance150;
    private IndexedTexturedSquare distance200;
    private IndexedTexturedSquare distance250;
    private IndexedTexturedSquare distance300;

    private Typeface typeface;

    private Bitmap calloutBg;

    private final float fstartAngle = 60f;
    private final float farcAndgle = (90 - fstartAngle) * 2;
    private final int numSegments = 20;
    private float startAngle = (float) VectorMath.deg2rad(fstartAngle);
    private float arcAngle = (float) VectorMath.deg2rad(farcAndgle);

    private FloatBuffer oneLineBuffer;
    private FloatBuffer twoLineBuffer;
    private ByteBuffer oneLineByteBuf = ByteBuffer.allocateDirect(this.twoLineB.length * 4);
    private ByteBuffer twoLinesByteBuf = ByteBuffer.allocateDirect(this.twoLineB.length * 4);

    public Callouts() {
    }

    public void init(Typeface typeface, int currentLocationResId, int cursorResId, Context context) {
        this.typeface = typeface;
        locationPolygon = new IndexedTexturedSquare(currentLocationResId, context, 1f);
        cursorPolygon = new IndexedTexturedSquare(cursorResId, context, 1f);

        color100 = Color.RED;
        color150 = Color.argb(255, 255, 255, 255);
        //color200 = Color.BLUE;
        color200 = Color.argb(255, 0, 210, 255); //#00D2FF
        color250 = Color.YELLOW;
        color300 = Color.argb(255, 89, 89, 89);

        distance100 = new IndexedTexturedSquare(context, R.string.distance_100, typeface, color100, GLES20.GL_CLAMP_TO_EDGE, 1.0f, false, false);
        distance150 = new IndexedTexturedSquare(context, R.string.distance_150, typeface, color150, GLES20.GL_CLAMP_TO_EDGE, 1.0f, false, false);
        distance200 = new IndexedTexturedSquare(context, R.string.distance_200, typeface, color200, GLES20.GL_CLAMP_TO_EDGE, 1.0f, false, false);
        distance250 = new IndexedTexturedSquare(context, R.string.distance_250, typeface, color250, GLES20.GL_CLAMP_TO_EDGE, 1.0f, false, false);
        distance300 = new IndexedTexturedSquare(context, R.string.distance_300, typeface, color300, GLES20.GL_CLAMP_TO_EDGE, 1.0f, false, false);
        calloutBg = BitmapFactory.decodeResource(context.getResources(), R.drawable.v2d_callout).copy(Bitmap.Config.ARGB_4444, true);

    }

    public void setCalloutsDrawMode(CalloutsDrawMode calloutsDrawMode) {
        this.calloutsDrawMode = calloutsDrawMode;
    }

    public void setFocus(boolean hasFocus) {
        this.hasFocus = hasFocus;
    }

    public boolean isHasFocus() {
        return hasFocus;
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
    }

    private void addLog(String currentLocation) {
        Log.e(getClass().getSimpleName(), currentLocation);
    }

    public void setCurrentLocation(Location currentLocation, Course3DRendererBase renderer) {
        if (currentLocation == null)
            return;

        double currentLon = Layer.transformToLon(twoLineB[0]);
        double currentLat = Layer.transformToLat(twoLineB[1]);
        Location location = new Location("");
        location.setLatitude(currentLat);
        location.setLongitude(currentLon);

        double distance = location.distanceTo(currentLocation);

        if (distance > 999)
            return;


        double x = Layer.transformLon(currentLocation.getLongitude());
        double y = Layer.transformLat(currentLocation.getLatitude());

        twoLineB[0] = (float) x;
        twoLineB[1] = (float) y;
        oneLineB[0] = (float) x;
        oneLineB[1] = (float) y;

        this.currentLocation = currentLocation;

        if (!this.hasFocus) {
            resetPosition();
        }

    }

    public void setCentralPath(PointListLayer centralP) {

        this.centralPath = centralP;

        if (centralPath == null)
            return;

        Vector start = centralPath.getFirstPointList().getFirstPoint();

        this.currentLocation = new Location("");
        currentLocation.setLatitude(start.y);
        currentLocation.setLongitude(start.x);

        twoLineB[0] = (float) start.x;
        twoLineB[1] = (float) start.y;
        twoLineB[2] = 0f;

        oneLineB[0] = (float) start.x;
        oneLineB[1] = (float) start.y;
        oneLineB[2] = 0f;

        Vector end = centralPath.getFirstPointList().getLastPoint();
        Vector middle = VectorMath.multiplied((VectorMath.added(start, end)), 0.5f);

        twoLineB[3] = (float) middle.x;
        twoLineB[4] = (float) middle.y;
        twoLineB[5] = 0f;

        twoLineB[6] = (float) end.x;
        twoLineB[7] = (float) end.y;
        twoLineB[8] = 0f;

        oneLineB[3] = (float) end.x;
        oneLineB[4] = (float) end.y;
        oneLineB[5] = 0f;

    }

    public void resetPosition() {
        Vector start = new Vector(twoLineB[0], twoLineB[1]);
        Vector end = new Vector(twoLineB[6], twoLineB[7]);
        Vector middle = VectorMath.multiplied(VectorMath.added(start, end), 0.5);

        twoLineB[3] = (float) middle.x;
        twoLineB[4] = (float) middle.y;
    }

    public void destroy() {
        if (distance100 != null)
            distance100.destroy();
        if (distance150 != null)
            distance150.destroy();
        if (distance200 != null)
            distance200.destroy();
        if (distance250 != null)
            distance250.destroy();
        if (distance300 != null)
            distance300.destroy();
        if (locationPolygon != null)
            locationPolygon.destroy();
        if (cursorPolygon != null)
            cursorPolygon.destroy();
    }


    public Vector getStartLocation() {
        Vector retval = null;
        if (calloutsDrawMode == CalloutsDrawMode.TWO_SEGMENTS)
            retval = new Vector(twoLineB[0], twoLineB[1]);
        else
            retval = new Vector(oneLineB[0], oneLineB[1]);

        return retval;

    }

    public Vector getEndLocation() {
        Vector retval = null;
        if (calloutsDrawMode == CalloutsDrawMode.TWO_SEGMENTS)
            retval = new Vector(twoLineB[6], twoLineB[7]);
        else
            retval = new Vector(oneLineB[3], oneLineB[4]);
        return retval;
    }

    public void draw(Course3DRenderer renderer) {
        if (renderer.getNavigationMode() != Course3DRendererBase.NavigationMode.NavigationMode2D) {
            return;
        }
        if (centralPath == null)
            return;
        if (calloutsDrawMode == null)
            calloutsDrawMode = CalloutsDrawMode.TWO_SEGMENTS;

        if (calloutsDrawMode == CalloutsDrawMode.ONE_SEGMENT)
            renderOneSegment(renderer);
        else
            renderTwoSegments(renderer);

    }

    private Location location1 = new Location("");
    private Location location2 = new Location("");
    private Location location3 = new Location("");
    private Location location4 = new Location("");

    IndexedTexturedSquare polygon;
    IndexedTexturedSquare polygonTS1;
    IndexedTexturedSquare polygonTS2;
    int prevDist = -1;
    int prevDistTS1 = -1;
    int prevDistTS2 = -1;

    Vector pt1;
    Vector pt2;
    Vector pt3;
    Vector cursorPoint;
    Vector pos1;
    Vector pos2;

    public void renderOneSegment(Course3DRenderer renderer) {
        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x, renderer.y, 0);
        renderer.bindMatrix();


        oneLineByteBuf.order(ByteOrder.nativeOrder());
        oneLineBuffer = oneLineByteBuf.asFloatBuffer();
        oneLineBuffer.put(this.oneLineB);
        oneLineBuffer.position(0);

        drawLines(renderer, oneLineBuffer, 2, 3, GLES20.GL_LINES, null);

        float locationScale = Math.abs(LOCATION_SCALE * renderer.z);
        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + this.oneLineB[0], renderer.y + this.oneLineB[1], 0);
        Matrix.scaleM(renderer.modelViewMatrix, 0, locationScale, locationScale, locationScale);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
        renderer.bindMatrix();
        locationPolygon.draw(renderer, 1);

        renderOverlay(renderer);


        location1.setLatitude(Layer.transformToLat(this.oneLineB[0]));
        location1.setLongitude(Layer.transformToLon(this.oneLineB[1]));
        location2.setLatitude(Layer.transformToLat(this.oneLineB[3]));
        location2.setLongitude(Layer.transformToLon(this.oneLineB[4]));


        int dist = (int) (renderer.distance(location1, location2) + 0.5);
        if (polygon == null)
            polygon = new IndexedTexturedSquare(-dist, String.valueOf(dist), calloutBg, typeface, color100, GLES20.GL_CLAMP_TO_EDGE, 1.0f, true, true);

        if (dist != prevDist) {
            polygon.updateTexture(-dist, String.valueOf(dist), calloutBg, typeface, color100, GLES20.GL_CLAMP_TO_EDGE, 1.0f, true);
            prevDist = dist;
        }

        if (pt1 == null)
            pt1 = new Vector();
        if (pt2 == null)
            pt2 = new Vector();

        pt1.x = this.oneLineB[0];
        pt1.y = this.oneLineB[1];
        pt2.x = this.oneLineB[3];
        pt2.y = this.oneLineB[4];

        pos1 = null;
        pos1 = calculateCalloutDrawPositionWithVector(renderer, pt1, pt2, null);

        float scale = Math.abs(CALLOUT_SCALE * renderer.z);
        if (pos1 != null) {
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
            Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + (float) pos1.x, renderer.y + (float) pos1.y, 0);
            Matrix.scaleM(renderer.modelViewMatrix, 0, scale, scale, scale);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
            renderer.bindMatrix();
            polygon.draw(renderer, 1);
            polygon.destroy();
            //TextureCache.destroyById(-dist);
        }
    }

    public void renderTwoSegments(Course3DRenderer renderer) {
        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x, renderer.y, 0);
        renderer.bindMatrix();

        twoLinesByteBuf.order(ByteOrder.nativeOrder());
        twoLineBuffer = twoLinesByteBuf.asFloatBuffer();
        twoLineBuffer.put(this.twoLineB);
        twoLineBuffer.position(0);

        drawLines(renderer, twoLineBuffer, 3, 3, GLES20.GL_LINE_STRIP, null);

        float locationScale = Math.abs(LOCATION_SCALE * renderer.z);
        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + this.twoLineB[0], renderer.y + this.twoLineB[1], 0);
        Matrix.scaleM(renderer.modelViewMatrix, 0, locationScale, locationScale, locationScale);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
        renderer.bindMatrix();
        locationPolygon.draw(renderer, 1);

        renderOverlay(renderer);

        float cursorScale = Math.abs(CURSOR_SCALE * renderer.z);
        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + this.twoLineB[3], renderer.y + this.twoLineB[4], 0);
        Matrix.scaleM(renderer.modelViewMatrix, 0, cursorScale, cursorScale, cursorScale);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
        renderer.bindMatrix();
        cursorPolygon.draw(renderer, 1);

        location1.setLatitude(Layer.transformToLat(this.twoLineB[0]));
        location1.setLongitude(Layer.transformToLon(this.twoLineB[1]));
        location2.setLatitude(Layer.transformToLat(this.twoLineB[3]));
        location2.setLongitude(Layer.transformToLon(this.twoLineB[4]));
        int dist = (int) (renderer.distance(location1, location2) + 0.5);

        if (polygonTS1 == null)
            polygonTS1 = new IndexedTexturedSquare(-dist, String.valueOf(dist), calloutBg, typeface, color100, GLES20.GL_CLAMP_TO_EDGE, 1.0f, true, true);

        if (dist != prevDistTS1) {
            polygonTS1.updateTexture(-dist, String.valueOf(dist), calloutBg, typeface, color100, GLES20.GL_CLAMP_TO_EDGE, 1.0f, true);
            prevDistTS1 = dist;
        }


        location3.setLatitude(Layer.transformToLat(this.twoLineB[3]));
        location3.setLongitude(Layer.transformToLon(this.twoLineB[4]));
        location4.setLatitude(Layer.transformToLat(this.twoLineB[6]));
        location4.setLongitude(Layer.transformToLon(this.twoLineB[7]));
        int dist2 = (int) (renderer.distance(location3, location4) + 0.5);

        if (polygonTS2 == null)
            polygonTS2 = new IndexedTexturedSquare(-dist2, String.valueOf(dist2), calloutBg, typeface, color100, GLES20.GL_CLAMP_TO_EDGE, 1.0f, true, true);

        if (dist != prevDistTS2) {
            polygonTS2.updateTexture(-dist2, String.valueOf(dist2), calloutBg, typeface, color100, GLES20.GL_CLAMP_TO_EDGE, 1.0f, true);
            prevDistTS2 = dist2;
        }

        if (pt1 == null)
            pt1 = new Vector();
        if (pt2 == null)
            pt2 = new Vector();
        if (pt3 == null)
            pt3 = new Vector();

        pt1.x = this.twoLineB[0];
        pt1.y = this.twoLineB[1];
        pt2.x = this.twoLineB[3];
        pt2.y = this.twoLineB[4];
        pt3.x = this.twoLineB[6];
        pt3.y = this.twoLineB[7];

        if (cursorPoint == null)
            cursorPoint = new Vector();

        cursorPoint.x = renderer.x + this.twoLineB[3];
        cursorPoint.y = renderer.x + this.twoLineB[4];

        pos1 = null;
        pos2 = null;
        pos1 = calculateCalloutDrawPositionWithVector(renderer, pt1, pt2, cursorPoint);
        pos2 = calculateCalloutDrawPositionWithVector(renderer, pt2, pt3, cursorPoint);

        float scale = Math.abs(CALLOUT_SCALE * renderer.z);
        if (pos1 != null) {
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
            Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + (float) pos1.x, renderer.y + (float) pos1.y, 0);
            Matrix.scaleM(renderer.modelViewMatrix, 0, scale, scale, scale);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
            renderer.bindMatrix();
            polygonTS1.draw(renderer, 1);
            polygonTS1.destroy();
        }

        if (pos2 != null) {
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
            Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + (float) pos2.x, renderer.y + (float) pos2.y, 0);
            Matrix.scaleM(renderer.modelViewMatrix, 0, scale, scale, scale);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
            renderer.bindMatrix();
            polygonTS2.draw(renderer, 1);
            polygonTS2.destroy();
        }

    }

    private void renderOverlay(Course3DRenderer renderer) {
        if (!showOverlay)
            return;

        double xstart = calloutsDrawMode == CalloutsDrawMode.TWO_SEGMENTS ? twoLineB[0] : oneLineB[0];
        double ystart = calloutsDrawMode == CalloutsDrawMode.TWO_SEGMENTS ? twoLineB[1] : oneLineB[1];

        double xend = calloutsDrawMode == CalloutsDrawMode.TWO_SEGMENTS ? twoLineB[6] : oneLineB[3];
        double yend = calloutsDrawMode == CalloutsDrawMode.TWO_SEGMENTS ? twoLineB[7] : oneLineB[4];

        double distanceWorld = VectorMath.distanceWithVector1(new Vector(xstart, ystart), new Vector(xend, yend));

        location1.setLatitude(Layer.transformToLat(xstart));
        location1.setLongitude(Layer.transformToLon(ystart));
        location2.setLatitude(Layer.transformToLat(xend));
        location2.setLongitude(Layer.transformToLon(yend));

        double dist = renderer.distance(location1, location2);
        double factor = distanceWorld / dist;
        if (dist > 100) {
            drawArcAndDistance(renderer, 100f, color100, (float) factor, (float) xstart, (float) ystart, distance100);
        }
        if (dist > 150) {
            drawArcAndDistance(renderer, 150f, color150, (float) factor, (float) xstart, (float) ystart, distance150);
        }
        if (dist > 200) {
            drawArcAndDistance(renderer, 200f, color200, (float) factor, (float) xstart, (float) ystart, distance200);
        }
        if (dist > 250) {
            drawArcAndDistance(renderer, 250f, color250, (float) factor, (float) xstart, (float) ystart, distance250);
        }
        if (dist > 300) {
            drawArcAndDistance(renderer, 300f, color300, (float) factor, (float) xstart, (float) ystart, distance300);
        }
    }

    private ArrayList<Vector> corners;

    private Vector calculateCalloutDrawPositionWithVector(Course3DRenderer renderer, Vector vector1, Vector vector2, Vector cursorPoint) {

        corners = null;
        corners = new ArrayList<>(Arrays.asList(renderer.unprojectViewport()));
        corners.add(corners.get(0));

        boolean v1inside = VectorMath.isVectorInsidePolygon(vector1, corners);
        boolean v2inside = VectorMath.isVectorInsidePolygon(vector2, corners);

        if (!v1inside && !v2inside) {
            Vector intersection1 = null;
            Vector intersection2 = null;

            for (int i = 0; i < corners.size() - 1; i++) {
                Vector intersetion = VectorMath.intersectionWithVector(vector1, vector2, corners.get(i), corners.get(i + 1));
                if (intersetion != null)
                    if (intersection1 == null) {
                        intersection1 = intersetion;
                    } else {
                        intersection2 = intersetion;
                        break;
                    }
            }

            if (intersection2 == null)
                return null;

            return new Vector(VectorMath.multiplied(VectorMath.added(intersection1, intersection2), 0.5));
        }

        if (v1inside && v2inside)
            return new Vector(VectorMath.multiplied(VectorMath.added(vector1, vector2), 0.5));

        corners.add(corners.get(0));

        Vector visiblePoint = v1inside ? vector1 : vector2;
        Vector invisiblePoint = v2inside ? vector1 : vector2;

        Vector intersction = null;

        for (int i = 0; i < corners.size() - 1; i++) {
            intersction = VectorMath.intersectionWithVector(visiblePoint, invisiblePoint, corners.get(i), corners.get(i + 1));
            if (intersction != null)
                break;
        }

        if (intersction == null)
            return null;
        return new Vector(VectorMath.multiplied(VectorMath.added(intersction, visiblePoint), 0.5));

    }

    private float[] lines = new float[numSegments * 2];

    private FloatBuffer linesBuffer;
    private ByteBuffer byteBuf;

    private void drawArcAndDistance(Course3DRenderer renderer, float distance, int color, float factor, float xpos, float ypos, IndexedTexturedSquare view) {
        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + xpos, renderer.y + ypos, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
        renderer.bindMatrix();

        float r = distance * factor;

        float step = arcAngle / numSegments;

        for (int i = 0; i < numSegments; i++) {

            float x = (float) Math.cos(startAngle + step * i);
            float y = (float) Math.sin(startAngle + step * i);

            x *= r;
            y *= r;

            lines[i * 2] = x;
            lines[i * 2 + 1] = y;
        }

        linesBuffer = null;
        byteBuf = null;
        byteBuf = ByteBuffer.allocateDirect(lines.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        linesBuffer = byteBuf.asFloatBuffer();
        linesBuffer.put(lines);
        linesBuffer.position(0);

        drawLines(renderer, linesBuffer, numSegments, 2, GLES20.GL_LINE_STRIP, color);

        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + xpos, renderer.y + ypos, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -fstartAngle / 2 + 2 - 0.005f * distance, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, r + 0.25f, 0);
        Matrix.scaleM(renderer.modelViewMatrix, 0, 0.3f, 0.3f, 0.3f);
        renderer.bindMatrix();

        view.draw(renderer, 1);

        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, renderer.z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.viewAngle, 1, 0, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, renderer.rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, renderer.x + xpos, renderer.y + ypos, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -renderer.rotationAngle, 0, 0, 1);
        Matrix.rotateM(renderer.modelViewMatrix, 0, fstartAngle / 2 - 3 - 0.005f * distance, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, r + 0.25f, 0);
        Matrix.scaleM(renderer.modelViewMatrix, 0, 0.3f, 0.3f, 0.3f);
        renderer.bindMatrix();

        view.draw(renderer, 1);

    }

    public boolean endMove(Vector coordinate) {
        if (calloutsDrawMode != CalloutsDrawMode.TWO_SEGMENTS || !hasFocus || coordinate == null)
            return false;

        hasFocus = false;

        twoLineB[3] = (float) coordinate.x;
        twoLineB[4] = (float) coordinate.y;

        return true;
    }

    public void startMove(Course3DRendererBase renderer, Vector coordinate) {
        if (calloutsDrawMode != CalloutsDrawMode.TWO_SEGMENTS || coordinate == null)
            return;
        double distance = VectorMath.distanceWithVector1(coordinate, new Vector(twoLineB[3], twoLineB[4]));
        if (distance < Math.abs(renderer.z * 0.03)) {
            hasFocus = true;
        }
    }

    public boolean onMove(Vector coordinate) {

        if (calloutsDrawMode != CalloutsDrawMode.TWO_SEGMENTS || !hasFocus || coordinate == null)
            return false;

        twoLineB[3] = (float) coordinate.x;
        twoLineB[4] = (float) coordinate.y;

        return true;
    }
}
