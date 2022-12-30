package com.l1inc.viewer;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.l1inc.viewer.common.Viewer;
import com.l1inc.viewer.drawing.Callouts;
import com.l1inc.viewer.drawing.CartDrawData;
import com.l1inc.viewer.drawing.TextureCache;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.elevation.Tile;
import com.l1inc.viewer.logging.DefaultViewerLogger;
import com.l1inc.viewer.logging.ViewerLogger;
import com.l1inc.viewer.textureset.TextureSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Yevhen Paschenko on 2/24/2016.
 */
public class Course3DViewer extends GLSurfaceView {

    private Course3DRenderer renderer;
    private ScaleGestureDetector scaleGestureDetector;
    private RotationGestureDetector rotationGestureDetector;
    private PanGestureDetector panGestureDetector;
    private GestureDetector doubleTapGestureDetector;
    private boolean anyGestureDetected = false;

    private int getFps() {
        return renderer.getFps();
    }

    public int getCurrentHole() {
        return renderer.getCurrentHole();
    }

    public Course3DRenderer.NavigationMode getNavigationMode() {
        return renderer.getNavigationMode();
    }

    public Course3DRenderer.NavigationMode getPendingNavigationMode() {
        return renderer.getPendingNavigationMode();
    }

    public void setNavigationMode(final Course3DRenderer.NavigationMode navigationMode) {
        renderer.setNavigationMode(navigationMode);
        requestRender();
    }

    public boolean isOverallMode() {
        return renderer.isOverallMode();
    }

    public boolean isOverallMode(final Course3DRenderer.NavigationMode mode) {
        return renderer.isOverallMode(mode);
    }

    public void setCalloutsDrawMode(final Callouts.CalloutsDrawMode mode) {
        renderer.setCalloutsDrawMode(mode);
        requestRender();
    }

    public Callouts.CalloutsDrawMode getCalloutsDrawMode() {
        return renderer.calloutsDrawMode;
    }

    public void setIsCalloutOverlay(boolean isOverlay) {
        renderer.setCalloutsOverlay(isOverlay);
        requestRender();
    }

    public void setNavigationModeChangedListener(final Viewer.NavigationModeChangedListener navigationModeChangedListener) {
        renderer.setNavigationModeChangedListener(navigationModeChangedListener);
        requestRender();
    }

    public void setFlyoverFinishListener(final Viewer.FlyoverFinishListener navigationModeChangedListener) {
        renderer.setFlyoverFinishListener(navigationModeChangedListener);
        //requestRender("5");
    }

    public void setGreenPositionChangeListener(final Viewer.GreenPositionChangeListener greenPositionChangeListener) {
        renderer.setGreenPositionChangeListener(greenPositionChangeListener);
        //requestRender("5");
    }

    public void setCurrentCourseChangedListener(final Viewer.CurrentCourseChangedListener currentCourseChangedListener) {
        renderer.setCurrentCourseChangedListener(currentCourseChangedListener);
        requestRender();
    }

    public void setCurrentHoleChangedListener(final Viewer.CurrentHoleChangedListener listener) {
        renderer.setCurrentHoleChangedListener(listener);
        //requestRender("7");
    }

    public void setHoleLoadingStateChangedListener(final Viewer.HoleLoadingStateChangedListener holeLoadingStateChangedListener) {
        renderer.setHoleLoadingStateChangedListener(holeLoadingStateChangedListener);
        //requestRender("8");
    }

    public void setCourseVectorElevationData(final Map<String, String> courseVectorData, Boolean resetElevations) {
        renderer.setCourseVectorDataMap(courseVectorData, resetElevations);
        //requestRender("9");
    }

    public void setFrameRenderListener(final Viewer.FrameRenderListener frameRenderListener) {
        renderer.setFrameRenderListener(frameRenderListener);
    }

    public void setCartsToDraw(ArrayList<CartDrawData> cartsArray){
        renderer.setCartsArray(cartsArray);
    }

    public void init(final Map<String, String> courseVectorData, Boolean resetElevations, boolean isMetricUnits, Map<String, Integer[]> parDataMap, TextureSet textureSet, boolean cartLocationVisible) {

        renderer.setCourseVectorDataMap(courseVectorData, resetElevations);
        renderer.setMetricUnits(isMetricUnits);
        renderer.setParDataMap(parDataMap);
        renderer.setTextureSet(textureSet);
        renderer.setCartLocationVisible(cartLocationVisible);

        //requestRender("9");

    }

    public void init(final Map<String, String> courseVectorData, Boolean resetElevations, boolean isMetricUnits, Map<String, Integer[]> parDataMap, TextureSet textureSet, boolean cartLocationVisible, Map<String, List<PinPositionOverride>> pinPositionOverrideMap) {

        renderer.setCourseVectorDataMap(courseVectorData, resetElevations);
        renderer.setMetricUnits(isMetricUnits);
        renderer.setParDataMap(parDataMap);
        renderer.setTextureSet(textureSet);
        renderer.setCartLocationVisible(cartLocationVisible);
        renderer.setPinPositionOverrideMap(pinPositionOverrideMap);

        //requestRender("9");

    }

    public void setFrontGreeenLocationGPS(final Location locationGPS) {
        renderer.setFrontGreeenLocationGPS(locationGPS);
        requestRender();
    }

    public void setBackGreeenLocationGPS(final Location locationGPS) {
        renderer.setBackGreeenLocationGPS(locationGPS);
        requestRender();
    }

    public void setHazardList(final List<Location> hazardList) {
        renderer.setHazardList(hazardList);
        requestRender();
    }

    public Location getPinPositionForCurrentHole() {
        return renderer.getOverridedPinPosition();
    }

    /*public void plusRotationAngle() {
        renderer.increaseRotationAngle();
        requestRender();
    }

    public void minusRotationAngle() {
        renderer.dicreaseRotationAngle();
        requestRender();
    }*/

    public int getNumHoles() {
        return renderer.getNumHoles();
    }

    public void enable3DBunkers(boolean enable) {
        renderer.setDraw3DBunkers(enable);
    }

    public boolean is3DBunkersEnabled() {
        return renderer.isDraw3DBunkers();
    }

    private void setHoleWithinCourse(final HoleWithinCourse holeWithinCourse,
                                     final boolean resetPosition) {
        renderer.setHoleWithinCourse(holeWithinCourse, resetPosition);
        requestRender();

    }

    public String getCourseID() {
        return renderer.getCourseID();
    }

    public HoleWithinCourse getHoleWithinCourse() {
        return renderer.getHoleWithinCourse();
    }

    public static void setViewerLogger(final ViewerLogger viewerLogger) {
        Course3DRendererBase.setViewerLogger(viewerLogger);
    }

    //NEW
    public void setCurrentHole(int currentHole,
                               Course3DRendererBase.NavigationMode navigationMode,
                               boolean resetPosition,
                               Integer initialTeeBox) {
        renderer.setCurrentHole(currentHole, navigationMode, resetPosition, initialTeeBox);
    }

    //NEW
    public void setCurrentHole(int currentHole,
                               boolean resetPosition,
                               Integer initialTeeBox) {
        renderer.setCurrentHole(currentHole, resetPosition, initialTeeBox);

    }

    private void setHoleWithinCourse(final HoleWithinCourse holeWithinCourse,
                                     final boolean resetPosition,
                                     final int initialTeeBox) {
        renderer.setHoleWithinCourse(holeWithinCourse, resetPosition, initialTeeBox);
    }

    public boolean setCurrentLocationGPS(final Location locationGPS,
                                         final boolean updateCameraPos) {

        boolean retval = renderer.setCurrentLocationGps(locationGPS, updateCameraPos);

        requestRender();
        return retval;

    }

    public void setParData(final Map<String, Integer[]> parData) {
        renderer.setParDataMap(parData);
        requestRender();
    }

    public void setMeasurementSystem(final boolean isMetricUnits) {
        renderer.setMetricUnits(isMetricUnits);
        requestRender();
    }

    public boolean isMetricUnits() {
        return renderer.isMetricUnits();
    }

    public void setAheadCartsListGPS(final List<Location> aheadCartsListGPS) {
        renderer.setAheadCartsListGps(aheadCartsListGPS);
        requestRender();
    }

    public void setPinPositionOverrides(final Map<String, List<PinPositionOverride>> overrideList) {
        renderer.setPinPositionOverrideMap(overrideList);
        requestRender();
    }

    public boolean isCurrentLocationVisible() {
        return renderer.isCurrentLocationVisible();
    }

    public void setTextureSet(final TextureSet textureSet) {
        renderer.setTextureSet(textureSet);
        requestRender();
    }

    public void setCartLocationVisible(final boolean visible) {
        renderer.setCartLocationVisible(visible);
        requestRender();
    }

    public void setTeeboxAsCurrentLocation(final int teeBoxIndex) {
        renderer.setTeeboxAsCurrentLocation(teeBoxIndex);
        requestRender();
    }

    public void onOrientationChanged() {
        renderer.onOrientationChanged();
    }


    private void onLocationOnScreenChanged(int[] location) {
        renderer.onLocationOnScreenChanged(location);
    }

    public void updateCart(Integer idCart, String cartName, Location cartLocation){
        renderer.updateCart(idCart, cartName, cartLocation);
        requestRender();
    }

    public void removeCart(Integer idCart){
        renderer.removeCart(idCart);
        requestRender();
    }

    public Course3DViewer(final Context context) {
        super(context);


        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        renderer = new Course3DRenderer(context, this);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);


        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(final ScaleGestureDetector detector) {
                anyGestureDetected = true;
                renderer.zoom(1 / detector.getScaleFactor());
                isPreviousScale = true;
                return false;
            }

            @Override
            public boolean onScaleBegin(final ScaleGestureDetector detector) {
                renderer.startZoom();
                isPreviousScale = true;
                return true;
            }

            @Override
            public void onScaleEnd(final ScaleGestureDetector detector) {
                renderer.endZoom();
                isPreviousScale = true;
            }
        });

        rotationGestureDetector = new RotationGestureDetector(new RotationGestureDetector.OnRotationGestureListener() {
            @Override
            public void OnRotationBegin(final RotationGestureDetector rotationDetector) {
                renderer.startRotation();
            }

            @Override
            public void OnRotation(final RotationGestureDetector rotationDetector) {
                anyGestureDetected = true;
                renderer.rotate(rotationDetector.getAngle());
            }

            @Override
            public void OnRotationEnd(final RotationGestureDetector rotationDetector) {
                renderer.endRotation();
            }
        });

        panGestureDetector = new PanGestureDetector(new PanGestureDetector.OnPanGestureListener() {
            @Override
            public void OnPanBegin(final PanGestureDetector panGestureDetector, MotionEvent event) {
                isPreviousScale = false;
                if (event != null && event.getPointerCount() > 1)
                    return;
                renderer.startMove(event.getX(), event.getY());
            }

            @Override
            public void OnPan(final PanGestureDetector panGestureDetector, MotionEvent event) {
                isPreviousScale = false;
                anyGestureDetected = true;
                if (event != null && event.getPointerCount() > 1)
                    return;
                renderer.move(panGestureDetector.getDeltaX(), panGestureDetector.getDeltaY(), event.getX(), event.getY());
            }

            @Override
            public void OnPanEnd(final PanGestureDetector panGestureDetector, MotionEvent event) {
                isPreviousScale = false;
                if (event != null && event.getPointerCount() > 1)
                    return;
                renderer.endMove(panGestureDetector.getDeltaX(), panGestureDetector.getDeltaY(), event.getX(), event.getY());
            }
        });

        doubleTapGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (renderer.getNavigationMode() == Course3DRendererBase.NavigationMode.NavigationMode2D) {
                    renderer.onDoubleTap(0.7f, e.getX(), e.getY());
                }
                isPreviousScale = false;
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                isPreviousScale = false;
                return false;
            }
        });

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    Tile.releaseTexturesAndFrameBuffers();
                    TextureCache.invalidate();
                    renderer.destroyAllData();
                    ElevationHelper.getInstance().prepare();
                    System.gc();
                }
            });
        } catch (Exception e) {

        }
    }

    private boolean isPreviousScale = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        requestRender();
        if (renderer.getNavigationMode() != Course3DRenderer.NavigationMode.Flyover) {
            if (renderer.getNavigationMode() == Course3DRendererBase.NavigationMode.NavigationMode2D) {
                if (scaleGestureDetector.onTouchEvent(event) && event.getPointerCount() == 1) {
                    if (isPreviousScale)
                        panGestureDetector.simulateTouchDown(event);
                    panGestureDetector.onTouchEvent(event);
                    //}
                }
            } else {
                panGestureDetector.onTouchEvent(event);
            }
            boolean isDoubleTap = false;
            if (renderer.getNavigationMode() == Course3DRendererBase.NavigationMode.NavigationMode2D)
                isDoubleTap = doubleTapGestureDetector.onTouchEvent(event);
            if (!isDoubleTap && event.getAction() == MotionEvent.ACTION_UP) {
                if (!anyGestureDetected && renderer.getNavigationMode() != Course3DRendererBase.NavigationMode.NavigationMode2D) {
                    renderer.tap(event.getX(), event.getY());
                }
                anyGestureDetected = false;
            }

        }
        requestRender();
        return true;
    }

}
