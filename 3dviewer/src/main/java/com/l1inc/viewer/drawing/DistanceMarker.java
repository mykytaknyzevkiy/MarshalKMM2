package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.Course3DViewer;
import com.l1inc.viewer.R;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.math.Vector;

import java.util.Comparator;

/**
 * Created by Yevhen Paschenko on 5/12/2016.
 */
public class DistanceMarker {

    final float[] modelViewMatrix = new float[16];
    final float[] modelViewProjectionMatrix = new float[16];
    float[] vIn = new float[]{0, 0, 0, 1};
    float[] vOut = new float[4];
    int textureId;

    public static final Comparator comparator = new Comparator<DistanceMarker>() {
        @Override
        public int compare(final DistanceMarker lhs, final DistanceMarker rhs) {
            return Float.compare(lhs.getZPosition(), rhs.getZPosition());
        }
    };

    private class MatrixCalculator {


        public void calculateMatrices(float x, float y, float z, float viewAngle, float rotationAngle, float[] projectionMatrix) {
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, 0);
            Matrix.multiplyMV(vOut, 0, modelViewMatrix, 0, vIn, 0);
            zPosition = vOut[2] / vOut[3];

            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
            Matrix.multiplyMV(vOut, 0, modelViewProjectionMatrix, 0, vIn, 0);
            projectedPosition.x = vOut[0] / vOut[3];
            projectedPosition.y = vOut[1] / vOut[3];
        }
    }

    private class BitmapGenerator {
        private Bitmap bitmap;
        private Canvas canvas;
        private Paint textPaint;
        private Paint strokePaint;

        public void destroy() {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        private void initResources() {
            bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_4444);
            canvas = new Canvas(bitmap);

            if (textPaint == null) {
                textPaint = new Paint();
                textPaint.setTextSize(100);
                textPaint.setAntiAlias(true);
                textPaint.setTypeface(typeface);
                textPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
                textPaint.setTextAlign(Paint.Align.CENTER);
            }

            if (strokePaint == null) {
                strokePaint = new Paint();
                strokePaint.setTextSize(100);
                strokePaint.setAntiAlias(true);
                strokePaint.setTypeface(typeface);
                strokePaint.setARGB(0xFF, 0x00, 0x00, 0x00);
                strokePaint.setTextAlign(Paint.Align.CENTER);
                strokePaint.setStyle(Paint.Style.STROKE);
                strokePaint.setStrokeWidth(1);
            }
        }

        private Bitmap getDistanceTextBitmap() {
            if (bitmap == null) {
                initResources();
            }

            bitmap.eraseColor(0x00000000);
            int xpos = canvas.getWidth() / 2;
            int ypos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
            canvas.drawText(String.valueOf(Math.round(distance)), xpos, ypos, textPaint);
            canvas.drawText(String.valueOf(Math.round(distance)), xpos, ypos, strokePaint);

            return bitmap;
        }
    }

    public static final float DEFAULT_SCALE = 0.5f;
    public static final float FREECAM_SCALE = 0.5f * 1.4f;
    public static final float OVERALL_SCALE = 1.0f * 1.4f;
    public static final float GREEN_VIEW_SCALE = 0.5f * 1.3f;

    private Location locationGps;
    private Vector position = new Vector(Double.MIN_VALUE, Double.MIN_VALUE);
    private float[] absolutePosition;
    private boolean initialized = false;
    private float distance;
    private IndexedTexturedSquare calloutMarker;
    private IndexedTexturedSquare groundMarker;
    private IndexedTexturedSquare distanceText;
    private boolean needToUpdateDistane = false;
    private Typeface typeface;
    private float ypos;
    private float markerScale = DEFAULT_SCALE;
    private float fontScale = DEFAULT_SCALE;
    private float zPosition;
    private Vector projectedPosition = new Vector();
    private boolean logged;
    private MatrixCalculator matrixCalculator = new MatrixCalculator();
    private BitmapGenerator bitmapGenerator = new BitmapGenerator();

    public boolean isInitialized() {
        return initialized;
    }

    public float getYpos() {
        return ypos;
    }

    public void setPosition(final Vector position) {
        this.position = new Vector(position);
    }

    public void setAbsolutePosition(final float[] absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    public void setDistance(final float distance) {
        if (this.distance != distance) {
            needToUpdateDistane = true;
        }

        this.distance = distance;
    }

    public void setLocationGps(final Location locationGps) {
        if (logged) {
            Course3DRenderer.getViewerLogger().info("DistanceMarker::setLocationGps old " + this.locationGps + " new " + locationGps);
        }

        this.locationGps = new Location(locationGps);

        recalculatePosition();


    }

    public void recalculatePosition() {
        if (locationGps == null) {
            return;
        }
        float y = (float) Layer.transformLat(this.locationGps.getLatitude());
        float x = (float) Layer.transformLon(this.locationGps.getLongitude());
        position = new Vector(x, y);

        if (logged) {
            Course3DRenderer.getViewerLogger().info("DistanceMarker::setLocationGps position " + position);
        }
    }

    public Location getLocationGps() {
        return locationGps;
    }

    public void setScale(final float markerScale) {
        this.fontScale = markerScale;
        this.markerScale = markerScale;
    }

    public Vector getProjectedPosition() {
        return projectedPosition;
    }

    public float getZPosition() {
        return zPosition;
    }

    public DistanceMarker() {
        logged = false;
    }

    public DistanceMarker(final boolean logged) {
        this.logged = logged;
    }

    public void initialize(final Context context,
                           final Typeface typeface) {
        calloutMarker = new IndexedTexturedSquare(R.drawable.v3d_gpsmap_callout, context, 1);
        this.typeface = typeface;

        initialized = true;
        textureId = TextureCache.getCustomDrawableId();
    }

    public void initialize(final Context context,
                           final int groundMarkerResId,
                           final Typeface typeface) {
        calloutMarker = new IndexedTexturedSquare(R.drawable.v3d_gpsmap_callout, context, 1);
        groundMarker = new IndexedTexturedSquare(groundMarkerResId, context, 1);
        this.typeface = typeface;
        textureId = TextureCache.getCustomDrawableId();
        initialized = true;
    }

    public void destroy() {
        bitmapGenerator.destroy();
        invalidate();
    }

    public void invalidate() {
        if (calloutMarker != null) {
            calloutMarker.destroy();
        }
        if (distanceText != null) {
            distanceText.destroy();
            distanceText = null;
        }
        if (groundMarker != null) {
            groundMarker.destroy();
        }

        initialized = false;
    }

    public void calculateMatrices(float x, float y, float z, float viewAngle, float rotationAngle, float[] projectionMatrix) {
        matrixCalculator.calculateMatrices(x, y, z, viewAngle, rotationAngle, projectionMatrix);
    }

    public void scaleByPosition(final float additionalFactor) {
        fontScale = (1.0f - (zPosition + 3.5f) / 7f) * additionalFactor; // here is magic
        markerScale = fontScale;
    }

    public void drawGroundMarker(Course3DRenderer renderer,
                                 final float viewAngle,
                                 final float rotationAngle,
                                 final float x,
                                 final float y,
                                 final float z) {

        if (!initialized) {
            return;
        }

        if (absolutePosition != null) {
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, absolutePosition[0], absolutePosition[1], absolutePosition[2]);
            Matrix.scaleM(renderer.modelViewMatrix, 0, 0.175f * markerScale, 0.175f * markerScale, 0.175f * markerScale);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            renderer.bindMatrix();
            groundMarker.draw(renderer, renderer.DEFAULT_OPACITY);
        } else {
            // draw callout
            position.z = ElevationHelper.getInstance().getHeightInPoint(position, renderer, true);
            if (position.z < 0)
                return;
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0); //perspective
            Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, (float) position.z);
            Matrix.scaleM(renderer.modelViewMatrix, 0, 0.175f * markerScale, 0.175f * markerScale, 0.175f * markerScale);
            renderer.bindMatrix();
            groundMarker.draw(renderer, renderer.DEFAULT_OPACITY);
        }
    }

    public void draw(Course3DRenderer renderer,
                     final float viewAngle,
                     final float rotationAngle,
                     final float x,
                     final float y,
                     final float z,
                     final boolean overlay) {

        if (!initialized) {
            return;
        }

        if (needToUpdateDistane) {
            needToUpdateDistane = false;

            if (distanceText == null) {
                distanceText = new IndexedTexturedSquare(1.0f);
            }
            //TODO CHECK
            distanceText.updateTexture(textureId, (int) (distance + 0.5), typeface);
        }

        if (absolutePosition != null) {
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, absolutePosition[0], absolutePosition[1], absolutePosition[2]);
            Matrix.scaleM(renderer.modelViewMatrix, 0, 0.2f * fontScale, 0.2f / 3 * fontScale, 1 * fontScale);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1, 0);
            renderer.bindMatrix();
            if (!overlay)
                renderer.enableDepth();
            calloutMarker.draw(renderer, renderer.DEFAULT_OPACITY);
            renderer.disableDepth();
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, absolutePosition[0], absolutePosition[1], absolutePosition[2]);
            Matrix.scaleM(renderer.modelViewMatrix, 0, 0.2f * fontScale, 0.2f * fontScale, 0.2f * fontScale);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1.25f, 0);
            renderer.bindMatrix();
            if (!overlay)
                renderer.enableDepth();
            distanceText.draw(renderer, renderer.DEFAULT_OPACITY);
            renderer.disableDepth();

        } else {
            // draw callout
            position.z = ElevationHelper.getInstance().getHeightInPoint(position, renderer, true);
            if (position.z < 0)
                return;
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0); //perspective
            Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, (float) position.z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
            Matrix.rotateM(renderer.modelViewMatrix, 0, viewAngle, 1, 0, 0); // rotate to camera
            Matrix.scaleM(renderer.modelViewMatrix, 0, 0.2f * fontScale, 0.2f / 3 * fontScale, 1 * fontScale);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1, 0);
            renderer.bindMatrix();
            if (!overlay)
                renderer.enableDepth();
            calloutMarker.draw(renderer, renderer.DEFAULT_OPACITY);
            if (!overlay)
                renderer.disableDepth();
            Matrix.setIdentityM(renderer.modelViewMatrix, 0);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -viewAngle, 1, 0, 0); //perspective
            Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(renderer.modelViewMatrix, 0, x + (float) position.x, y + (float) position.y, (float) position.z);
            Matrix.rotateM(renderer.modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
            Matrix.rotateM(renderer.modelViewMatrix, 0, viewAngle, 1, 0, 0); // rotate to camera
            Matrix.scaleM(renderer.modelViewMatrix, 0, 0.2f * fontScale, 0.2f * fontScale, 0.2f * fontScale);
            Matrix.translateM(renderer.modelViewMatrix, 0, 0, 1.25f, 0);
            renderer.bindMatrix();
            if (distanceText == null)
                return;
            if (!overlay)
                renderer.enableDepth();
            distanceText.draw(renderer, renderer.DEFAULT_OPACITY);
            if (!overlay)
                renderer.disableDepth();
        }
    }

    private Bitmap getDistanceTextBitmap() {
        return bitmapGenerator.getDistanceTextBitmap();
    }
}
