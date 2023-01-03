package com.l1inc.viewer;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.l1inc.viewer.drawing.Bunker3DPolygon;
import com.l1inc.viewer.drawing.Callouts;
import com.l1inc.viewer.drawing.CartDrawData;
import com.l1inc.viewer.drawing.CartPath;
import com.l1inc.viewer.drawing.Constants;
import com.l1inc.viewer.drawing.Creek;
import com.l1inc.viewer.drawing.CreekRenderer;
import com.l1inc.viewer.drawing.DistanceMarker;
import com.l1inc.viewer.drawing.Ground;
import com.l1inc.viewer.drawing.IDrawable;
import com.l1inc.viewer.drawing.IndexedTexturedSquare;
import com.l1inc.viewer.drawing.Layer;
import com.l1inc.viewer.drawing.OutlineLayer;
import com.l1inc.viewer.drawing.PointList;
import com.l1inc.viewer.drawing.PointListLayer;
import com.l1inc.viewer.drawing.Sky;
import com.l1inc.viewer.drawing.TextureCache;
import com.l1inc.viewer.drawing.Tree;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.elevation.Tile;
import com.l1inc.viewer.logging.DefaultViewerLogger;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.QuickSort;
import com.l1inc.viewer.math.Ray;
import com.l1inc.viewer.math.Triangle;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;
import com.l1inc.viewer.parcer.BaseShapeObject;
import com.l1inc.viewer.parcer.VectorDataObject;
import com.l1inc.viewer.parcer.VectorGPSObject;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Yevhen Paschenko on 2/21/2016.
 */
public class Course3DRenderer extends Course3DRendererBase implements GLSurfaceView.Renderer {


    static {
        System.loadLibrary("triangulation");
    }

    public Course3DRenderer(final Context context, Course3DViewer mainView) {
        this.context = context;
        this.glSurfaceView = mainView;
        callouts = new Callouts();
        setViewerLogger(new DefaultViewerLogger());


    }

    @Override
    public synchronized void onSurfaceCreated(final GL10 gl,
                                              final EGLConfig config) {
        proceedOnSurfaceCreated();

    }

    private void proceedOnSurfaceCreated() {
        Tile.releaseTexturesAndFrameBuffers();
        Tree.invalidate();
        TextureCache.invalidate();
        invalidateHazardMarkers();

        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(true);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glLineWidth(3);
        createAndUseProgram();
        locationMarker = new IndexedTexturedSquare(R.drawable.v3d_gpsmap_viewercurrentloc, context, 1);
        aheadCartMarker = new IndexedTexturedSquare(R.drawable.v3d_cart_location_pin, context, 1);
        callouts.init(typeface, R.drawable.v2d_current_location, R.drawable.v2d_cursor, context);
        callouts.setCalloutsDrawMode(calloutsDrawMode);
        callouts.setShowOverlay(calloutsShowOverlay);
        bind2DGround();
    }

    private synchronized void loadCourseData() {
        System.gc();

        if (layerList != null)
            layerList.clear();

        updateDistanceMarkers();
        flagPolygon = new IndexedTexturedSquare(R.drawable.v3d_gpsmap_flag, context, 1);

        layerList = new ArrayList<>();

        try {

            vectorGPSObject = ElevationHelper.getInstance().getGson().fromJson(courseVectorDataMap.get(holeWithinCourse.getCourseId()), VectorDataObject.class);

            if (vectorGPSObject.getVectorGPSObject() == null) {
                vectorGPSObject.setVectorGPSObject(ElevationHelper.getInstance().getGson().fromJson(courseVectorDataMap.get(holeWithinCourse.getCourseId()), VectorGPSObject.class));
            }
            numHoles = vectorGPSObject.getVectorGPSObject().getHoleCount();
            if (currentCourseChangedListener != null) {
                currentCourseChangedListener.onCourseChanged();
            }
            setHoleWithinCourse(holeWithinCourse, true, initialTeeBox);
        } catch (Exception e) {
            e.printStackTrace();
            getViewerLogger().error(e);
        }


        int totalGroundCount = 1;

        setTextureSet();
        if (elevationDataExist) {

            int mapHeight = ElevationHelper.getInstance().getMapHeight();
            int mapWidth = ElevationHelper.getInstance().getMapWidth();

            int maxIndex = mapWidth - 2 + (mapHeight - 2) * mapWidth;
            totalGroundCount = (int) Math.ceil(maxIndex / Short.MAX_VALUE) + 1;
            int rowsInOneGround = mapHeight / totalGroundCount;
            int startRow = 0;
            int dotStartCount = 0;
            ground = new Ground[totalGroundCount];
            boolean isLast = false;
            for (int i = 0; i < ground.length; i++) {
                isLast = i == ground.length - 1;
                ground[i] = new Ground(this, getBackgroundTextureId(), getBackgroundTextureId2D(), context, startRow, mapHeight, rowsInOneGround, i, mapHeight - startRow, isLast, dotStartCount);
                dotStartCount = ground[i].dotCount;
                startRow += rowsInOneGround;
            }

        } else {
            ground = new Ground[totalGroundCount];
            ground[0] = new Ground(this, getBackgroundTextureId(), getBackgroundTextureId2D(), context);
        }

        try {
            skyGradient = new Sky(R.drawable.v3d_sky, context);
            skyClouds = new Sky(R.drawable.v3d_clouds, context);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    private void loadStaticData() throws JSONException {

        sandBorderLayer = loadLayer(vectorGPSObject.getVectorGPSObject().getSand(), "Sand", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_sand_border : R.raw.v3d_gpsmap_sand_border, 0.05f,
                Layer.PointInterpolation.Interpolate, ETC1Util.isETC1Supported());
        sandLayer = loadLayer(vectorGPSObject.getVectorGPSObject().getSand(), "Sand", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_sand : R.raw.v3d_gpsmap_sand, 0f,
                Layer.PointInterpolation.Interpolate, ETC1Util.isETC1Supported());

        lakeBorderLayer = addLayer(vectorGPSObject.getVectorGPSObject().getLake(), "Lake", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_lake_border : R.raw.v3d_gpsmap_lake_border, 0.05f,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());
        lakeLayer = addLayer(vectorGPSObject.getVectorGPSObject().getLake(), "Lake", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_lake : R.raw.v3d_gpsmap_lake,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());

        lavaLayer = addLayer(vectorGPSObject.getVectorGPSObject().getLava(), "Lava", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_lava : R.raw.v3d_gpsmap_lava, ETC1Util.isETC1Supported());

        oceanBorderLayer = addLayer(vectorGPSObject.getVectorGPSObject().getOcean(), "Ocean", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_ocean_border : R.raw.v3d_gpsmap_ocean_border, 0.05f,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());
        oceanLayer = addLayer(vectorGPSObject.getVectorGPSObject().getOcean(), "Ocean", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_ocean : R.raw.v3d_gpsmap_ocean,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());

        pondBorderLayer = addLayer(vectorGPSObject.getVectorGPSObject().getPond(), "Pond", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_pond_border : R.raw.v3d_gpsmap_pond_border, 0.05f,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());
        pondLayer = addLayer(vectorGPSObject.getVectorGPSObject().getPond(), "Pond", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_pond : R.raw.v3d_gpsmap_pond,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());

        waterBorderLayer = addLayer(vectorGPSObject.getVectorGPSObject().getWater(), "Water", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_pond_border : R.raw.v3d_gpsmap_pond_border, 0.05f,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());
        waterLayer = addLayer(vectorGPSObject.getVectorGPSObject().getWater(), "Water", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_pond : R.raw.v3d_gpsmap_pond,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());

        bridgeLayer = addLayer(vectorGPSObject.getVectorGPSObject().getBridge(), "Bridge", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_bridge : R.raw.v3d_gpsmap_bridge,
                Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());

        try {
            if (vectorGPSObject.getVectorGPSObject().getTree() != null) {
                tree = new PointListLayer(vectorGPSObject.getVectorGPSObject().getTree(), true);
            }
        } catch (Exception e) {
            getViewerLogger().info("no tree layer found");
            e.printStackTrace();
        }

        try {
            if (vectorGPSObject.getVectorGPSObject().getPath() != null)
                path = new PointListLayer(vectorGPSObject.getVectorGPSObject().getPath(), true);
        } catch (Exception e) {
            getViewerLogger().info("no path layer found");
            e.printStackTrace();
        }

        try {
            if (vectorGPSObject.getVectorGPSObject().getCreek() != null)
                creek = new PointListLayer(vectorGPSObject.getVectorGPSObject().getCreek(), true);
        } catch (JSONException e) {
            getViewerLogger().info("no creek layer found");
            e.printStackTrace();
        }

    }

    private void loadHole() {
        viewShift = 0;
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        clearExtremePoints();

        for (final IDrawable layer : layerList) {
            if (layer != null)
                layer.destroy();
        }
        layerList.clear();

        if (treeArray != null) {
            for (final Tree tree : treeArray) {
                tree.destroy();
            }
        }

        treeArray = null;

        for (final CartPath cartPath : cartPathList) {
            cartPath.destroy();
        }
        cartPathList.clear();

        if (bunker3DLayer != null)
            bunker3DLayer.destroy();

        tapDistanceCreateTime = 0;
        stopTapDistanceMarkerHandler();
        if (tapDistanceMarker != null) {
            tapDistanceMarker.destroy();
            tapDistanceMarker = null;
        }

        //TODO REMOVED
        ElevationHelper.getInstance().clearVBO();

        System.gc();

        try {

            holeObject = vectorGPSObject.getVectorGPSObject().getHoles().getHoles().get(holeWithinCourse.getHoleNumber() - 1);

            // perimeter
            perimeter = addLayer(holeObject.getPerimeter(), "Perimeter", getPerimeterTextureId(), ETC1Util.isETC1Supported());
            // fairway
            fairway = null;
            BaseShapeObject fairwayObject = holeObject.getFairway();

            addLayer(fairwayObject, "Fairway", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_fairway_border : R.raw.v3d_gpsmap_fairway_border, 0.1f,
                    Layer.PointInterpolation.Interpolate, ETC1Util.isETC1Supported());
            Layer fairwayLayer = addLayer(fairwayObject, "Fairway", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_fairway : R.raw.v3d_gpsmap_fairway, ETC1Util.isETC1Supported());
            if (fairwayLayer != null) {
                fairway = new PointListLayer(fairwayObject, true);
            }
            // bunker
            addLayer(holeObject.getBunker(), "Bunker", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_bunker : R.raw.v3d_gpsmap_bunker, ETC1Util.isETC1Supported());
            addBunkerLayer(holeObject.getBunker(), "Bunker", !ETC1Util.isETC1Supported() ? R.drawable.bunker_background : R.raw.bunker_background, ETC1Util.isETC1Supported());
            // green
            green = addLayer(holeObject.getGreen(), "Green", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_green : R.raw.v3d_gpsmap_green, ETC1Util.isETC1Supported());
            extremeGreenTop = green.getExtremeTop();
            extremeGreenBottom = green.getExtremeBottom();
            extremeGreenLeft = green.getExtremeLeft();
            extremeGreenRight = green.getExtremeRight();
            if (needToLoadStaticData) {
                needToLoadStaticData = false;
                loadStaticData();
            }


            addOutlineLayer(holeObject.getBunker(), "Bunker", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_bunker_border : R.raw.v3d_gpsmap_bunker_border, 0.025f,
                    Layer.PointInterpolation.Interpolate, ETC1Util.isETC1Supported());
            addOutlineLayer(holeObject.getBunker(), "Bunker3DBorder", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_bunker_border : R.raw.v3d_gpsmap_bunker_border, 0.11f,
                    Layer.PointInterpolation.Interpolate, ETC1Util.isETC1Supported());


            if (sandBorderLayer != null) {
                updateExtremePoints(sandBorderLayer);
                layerList.add(sandBorderLayer);
            }

            if (sandLayer != null) {
                updateExtremePoints(sandLayer);
                layerList.add(sandLayer);
            }

            if (lakeBorderLayer != null) {
                updateExtremePoints(lakeBorderLayer);
                layerList.add(lakeBorderLayer);
            }
            if (lakeLayer != null) {
                updateExtremePoints(lakeLayer);
                layerList.add(lakeLayer);
            }
            if (lavaLayer != null) {
                updateExtremePoints(lavaLayer);
                layerList.add(lavaLayer);
            }
            if (oceanBorderLayer != null) {
                updateExtremePoints(oceanBorderLayer);
                layerList.add(oceanBorderLayer);
            }
            if (oceanLayer != null) {
                updateExtremePoints(oceanLayer);
                layerList.add(oceanLayer);
            }
            if (pondBorderLayer != null) {
                updateExtremePoints(pondBorderLayer);
                layerList.add(pondBorderLayer);
            }
            if (pondLayer != null) {
                updateExtremePoints(pondLayer);
                layerList.add(pondLayer);
            }
            if (waterBorderLayer != null) {
                updateExtremePoints(waterBorderLayer);
                layerList.add(waterBorderLayer);
            }
            if (waterLayer != null) {
                updateExtremePoints(waterLayer);
                layerList.add(waterLayer);
            }

            // teebox
            addLayer(holeObject.getTeebox(), "Teebox", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_teebox_border : R.raw.v3d_gpsmap_teebox_border, Constants.SCALE_005,
                    Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());
            addLayer(holeObject.getTeebox(), "Teebox", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_teebox : R.raw.v3d_gpsmap_teebox,
                    Layer.PointInterpolation.KeepOriginal, ETC1Util.isETC1Supported());

            // green
            OutlineLayer gB = addOutlineLayer(holeObject.getGreen(), "Green", !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_green_border : R.raw.v3d_gpsmap_green_border, Constants.SCALE_005,
                    Layer.PointInterpolation.Interpolate, ETC1Util.isETC1Supported());
            eGB = gB.getExtremeBottom();
            eGL = gB.getExtremeLeft();
            eGR = gB.getExtremeRight();
            eGT = gB.getExtremeTop();
            eGB.z = (ElevationHelper.getInstance().getHeightInPoint(eGB, this));
            eGL.z = (ElevationHelper.getInstance().getHeightInPoint(eGL, this));
            eGR.z = (ElevationHelper.getInstance().getHeightInPoint(eGR, this));
            eGT.z = (ElevationHelper.getInstance().getHeightInPoint(eGT, this));

            if (creek != null) {
                final List<Creek> creekList = new ArrayList<>();
                for (final PointList pointList : creek.getPointList()) {
                    if (!RectF.intersects(perimeter.getBoundingBox(), pointList.getBoundingBox())) {
                        continue;
                    }
                    final Creek creek = new Creek(R.drawable.v3d_gpsmap_creek, context, pointList, Constants.SCALE_01);
                    updateExtremePoints(creek);
                    creekList.add(creek);
                }
                layerList.add(new CreekRenderer(creekList));
            }

            if (bridgeLayer != null) {
                updateExtremePoints(bridgeLayer);
                layerList.add(bridgeLayer);
            }

            // greenCenter
            greencenter = new PointListLayer(holeObject.getGreenCenter(), true);

            // centralpath
            centralpath = new PointListLayer(holeObject.getCentralpath(), true);


            if (path != null) {
                for (final PointList pointList : path.getPointList()) {
                    if (!RectF.intersects(perimeter.getBoundingBox(), pointList.getBoundingBox())) {
                        continue;
                    }
                    cartPath = null;
                    cartPath = new CartPath(R.drawable.v3d_gpsmap_cart_path, context, pointList, Constants.SCALE_01);
                    updateExtremePoints(cartPath);
                    cartPathList.add(cartPath);
                }
            }

            final Vector vgreencenter = greencenter.getFirstPointList().getFirstPoint();
            final Vector vcentralpath1 = centralpath.getFirstPointList().getFirstPoint();

            final Vector vcentralpath2 = centralpath.getFirstPointList().getLastPoint();
            final double len1 = vcentralpath1.distance(vgreencenter);
            final double len2 = vcentralpath2.distance(vgreencenter);
            if (len1 < len2) {
                Collections.reverse(Arrays.asList(centralpath.getFirstPointList().getPointList()));
            }

            try {
                if (getOverridedPinPosition() != null) {
                    Location overrided = getOverridedPinPosition();
                    float lat = (float) Layer.transformLat(overrided.getLatitude());
                    float lon = (float) Layer.transformLon(overrided.getLongitude());
                    centralpath.getFirstPointList().setLastPoint(new Vector(lon, lat));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            callouts.setCentralPath(centralpath);

            // perimeterPointListLayer
            perimeterPointListLayer = new PointListLayer(holeObject.getPerimeter(), true);

            final int treeTextures[] = loadIntArray(textureSet.getTreeTextureSet(), context);
            final int treeShadowTextures[] = loadIntArray(textureSet.getTreeShadowTextureSet(), context);
            if (treeTextures.length != treeShadowTextures.length) {
                throw new RuntimeException("number of tree textures does not match the number of tree shadow textures");
            }
            final Random random = new Random(System.currentTimeMillis());

            final List<Tree> trees = new ArrayList<>();
            if (tree != null) {
                for (final PointList pointList : tree.getPointList()) {
                    for (final Vector vector : pointList.getPointList()) {
                        if (!perimeter.getBoundingBox().contains((float) vector.x, (float) vector.y)) {
                            continue;
                        }

                        final int textureIndex = random.nextInt(treeTextures.length);
                        final int textureId = treeTextures[textureIndex];
                        final int shadowTextureId = treeShadowTextures[textureIndex];
                        final Tree treeObject = new Tree(context, textureId, R.drawable.v2d_tree, shadowTextureId, vector, this);
                        trees.add(treeObject);
                    }
                }
            }

            treeArray = trees.toArray(new Tree[trees.size()]);

            Location overridedPinPosition = getOverridedPinPosition();
            if (overridedPinPosition == null) {
                hasPinOverride = false;

                final float flagX = (float) greencenter.getPointList().get(0).getPointList()[0].x;
                final float flagY = (float) greencenter.getPointList().get(0).getPointList()[0].y;
                flagLocation = new Location("");
                flagLocation.setLongitude(flagX);
                flagLocation.setLatitude(flagY);

                PointListLayer greencenterGPS = new PointListLayer(holeObject.getGreenCenter(), false);
                final float flagXGPS = (float) greencenterGPS.getPointList().get(0).getPointList()[0].x;
                final float flagYGPS = (float) greencenterGPS.getPointList().get(0).getPointList()[0].y;
                flagLocationGPS = new Location("");
                flagLocationGPS.setLongitude(flagXGPS);
                flagLocationGPS.setLatitude(flagYGPS);
            } else {
                hasPinOverride = true;

                final float flagY = (float) Layer.transformLat(overridedPinPosition.getLatitude());
                final float flagX = (float) Layer.transformLon(overridedPinPosition.getLongitude());
                flagLocation = new Location("");
                flagLocation.setLongitude(flagX);
                flagLocation.setLatitude(flagY);
                flagLocationGPS = overridedPinPosition;
            }

            teeboxcenter = new PointListLayer(holeObject.getTeeboxcenter(), true);
            teeboxcenterGPS = new PointListLayer(holeObject.getTeeboxcenter(), false);
            if (resetPosition) {
                setTeeboxAsCurrentLocation();
            } else {
                // set location to recalculate view angle
                setCurrentLocationGPSInternal(currentLocationGPS, true, true, false);
            }


            finalPosition = greencenter.getFirstPointList().getFirstPoint();

            ElevationHelper.getInstance().setStartLayerIndexes(new Vector(extremeLayersLeft.x, extremeLayersTop.y), new Vector(extremeLayersRight.x, extremeLayersTop.y), new Vector(extremeLayersLeft.x, extremeLayersBottom.y), new Vector(extremeLayersRight.x, extremeLayersBottom.y));

            prepareFlyoverParameters();

            if (holeLoadingStateChangedListener != null) {
                holeLoadingStateChangedListener.onFinishLoading();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void onSurfaceChanged(final GL10 gl,
                                              final int width,
                                              final int height) {
        viewportWidth = width;
        viewportHeight = height;

        final float zNear = 0.1f;
        final float zFar = 5000.0f;
        final float aspect = (float) width / (float) height;
        final float fovy = 45.0f;

        gl.glViewport(0, 0, width, height);
        Matrix.setIdentityM(projectionMatrix, 0);
        Matrix.perspectiveM(projectionMatrix, 0, fovy, aspect, zNear, zFar);
        final float top = zNear * (float) Math.tan(fovy * (Math.PI / 360.0));
        final float bottom = -top;
        final float left = bottom * aspect;
        final float right = top * aspect;
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, zNear, zFar);
        viewport = new Point(width, height);
        bindIndexedTextureSquareBuffers();

    }

    private void initProjectionMatrix() {

        final float zNear = 0.1f;
        final float zFar = 5000.0f;
        final float aspect = (float) viewportWidth / (float) viewportHeight;
        final float fovy = 45.0f;

        GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
        Matrix.setIdentityM(projectionMatrix, 0);
        Matrix.perspectiveM(projectionMatrix, 0, fovy, aspect, zNear, zFar);
        final float top = zNear * (float) Math.tan(fovy * (Math.PI / 360.0));
        final float bottom = -top;
        final float left = bottom * aspect;
        final float right = top * aspect;
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, zNear, zFar);
        viewport = new Point(viewportWidth, viewportHeight);
        bindIndexedTextureSquareBuffers();
    }

    private void setCurrentTilesByCameraPos() {

        boolean tilesChanged = false;

        if (navigationMode != null && navigationMode != NavigationMode.NavigationMode2D && elevationDataExist && ElevationHelper.getInstance().tilesExist()) {
            ElevationHelper.getInstance().setCurrentTileByCameraPositionAndShift(this);
            try {
                for (int position : ElevationHelper.getInstance().getDrawTiles().keySet()) {

                    tile = ElevationHelper.getInstance().getTileByPosition(position);
                    if (!needRedrawTiles)
                        if (tile == null || !tile.setTextureQuality(ElevationHelper.getInstance().getDrawTiles().get(position), position))
                            continue;
                    // current++;
                    if (!tilesChanged) {

                        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
                        GLES20.glDepthMask(false);
                        GLES20.glUniform1i(texSampler, 0);
                        GLES20.glUniform1f(opacity, 1);
                        GLES20.glUniform1f(isTexture, 1);
                        GLES20.glEnableVertexAttribArray(vTexCoord);
                        GLES20.glEnableVertexAttribArray(aPositionLocation);
                        GLES20.glClearColor(0, 0, 0, 0);

                    }
                    tilesChanged = true;
                    renderToTextureAtPosition();
                }
                if (needRedrawTiles)
                    needRedrawTiles = false;
            } catch (Exception e) {
                Course3DRenderer.getViewerLogger().error(e.toString());
                e.printStackTrace();
            }

            if (!tilesChanged)
                return;

            final float zNear = 0.1f;
            final float zFar = Constants.CAMERA_MAX_VALUE;
            final float aspect = (float) viewportWidth / (float) viewportHeight;
            final float fovy = 45.0f;

            GLES20.glViewport(0, 0, viewportWidth, viewportHeight);
            Matrix.setIdentityM(projectionMatrix, 0);
            Matrix.perspectiveM(projectionMatrix, 0, fovy, aspect, zNear, zFar);
            final float top = zNear * (float) Math.tan(fovy * (Math.PI / 360.0));
            final float bottom = -top;
            final float left = bottom * aspect;
            final float right = top * aspect;
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, zNear, zFar);
            GLES20.glClearColor(0, 0, 0, 0);

        }
    }

    private void renderToTextureAtPosition() {
        int width = tile.getViewPortWidth();
        int height = tile.getViewPortHeight();

        final float zNear = 0.1f;
        final float zFar = Constants.CAMERA_MAX_VALUE;
        final float aspect = Math.abs((float) width / (float) height);
        final float fovy = 45.0f;

        GLES20.glViewport(0, 0, width, height);
        Matrix.setIdentityM(projectionMatrix, 0);
        Matrix.perspectiveM(projectionMatrix, 0, fovy, aspect, zNear, zFar);
        float top = zNear * (float) Math.tan(fovy * (Math.PI / 360.0));
        float bottom = -top;
        float left = bottom * aspect;
        float right = top * aspect;
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, zNear, zFar);

        applyVBOZoom(tile);

        float retvalX = currentXPosVBO;
        float retvalY = currentYPosVBO;
        float retvalZ = currentZPosVBO;

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.translateM(modelViewMatrix, 0, 0, 0, retvalZ);
        Matrix.rotateM(modelViewMatrix, 0, 0, 1, 0, 0);
        Matrix.rotateM(modelViewMatrix, 0, 0, 0, 0, 1);
        Matrix.translateM(modelViewMatrix, 0, retvalX, retvalY, 0);
        bindMatrix();

        Frustum.updateFrustum(modelViewMatrix, projectionMatrix);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, tile.getFboId());

        tile.prepareForRenderToTexture();

        GLES20.glUniform1f(opacity, 1);

        for (int j = 0; j < layerList.size(); j++) {
            if (textureSetType == TextureSetType.TROPICAL || textureSetType == TextureSetType.CUSTOM) {
                if (draw3DBunkers) {
                    if ((layerList.get(j).getName() != null && layerList.get(j).getName().toLowerCase().equals("bunker")) || layerList.get(j).getName() != null && layerList.get(j).getName().toLowerCase().equals("bunker outline"))
                        continue;
                } else {
                    if ((layerList.get(j).getName() != null && layerList.get(j).getName().toLowerCase().equals("bunker3dborder outline")))
                        continue;
                }
                layerList.get(j).draw(this);
            } else if (!(layerList.get(j).getName() != null && layerList.get(j).getName().toLowerCase().equals("perimeter")))
                if (draw3DBunkers) {
                    if ((layerList.get(j).getName() != null && layerList.get(j).getName().toLowerCase().equals("bunker")) || layerList.get(j).getName() != null && layerList.get(j).getName().toLowerCase().equals("bunker outline"))
                        continue;
                } else {
                    if ((layerList.get(j).getName() != null && layerList.get(j).getName().toLowerCase().equals("bunker3dborder outline")))
                        continue;
                }
            layerList.get(j).draw(this);
        }

        for (int k = 0; k < cartPathList.size(); k++) {
            cartPathList.get(k).draw(this);
        }

        if (drawShadowsToTiles) {

            //enableDepth();
            if (treeArray != null)
                for (final Tree treeObject : treeArray) {
                    treeObject.calculateMatrices(retvalX, retvalY, retvalZ, 0, 0, projectionMatrix);
                }
            QuickSort.sortInplace(treeArray, Tree.comparator);
            if (navigationMode != NavigationMode.NavigationMode2D && treeArray != null)
                for (Tree aTreeArray : treeArray) {
                    aTreeArray.drawShadowToFBO(this, 0, 0, tile.getTilePosition2D());
                }

            // disableDepth();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }

    private double viewBone;
    private float previousZ;

    @Override
    public synchronized void onDrawFrame(final GL10 gl) {

        if (isDetroying)
            return;

        if (courseVectorDataMap == null) {
            return;
        }

        if (needToProccedOnSurfaceCreated) {
            needToProccedOnSurfaceCreated = false;
            proceedOnSurfaceCreated();
        }
        if (holeWithinCourse == null)
            return;

        if (needToLoadCourseData) {
            needToLoadCourseData = false;
            loadCourseData();
        }

        if(getHoleWithinCourse()!=null && getHoleWithinCourse().getHoleNumber() < 1 || getHoleWithinCourse().getHoleNumber() > numHoles){
            if(currentHoleChangedListener!=null)
            currentHoleChangedListener.onHoleFailed();
            return;
        }

        if (needToChangeHole) {
            needToChangeHole = false;
            loadHole();
        }

        if (needToChangeNavigationMode) {
            needToChangeNavigationMode = false;
            previousNavigationMode = navigationMode;
            navigationMode = newNavigationMode;

            if (previousNavigationMode == NavigationMode.Flyover && navigationMode == NavigationMode.FlyoverPause) {
                flyoverController.tick();
                Vector pos = flyoverController.pause();

                currentRotationAngle = (float) flyoverController.getRotationAngle();
                currentViewAngle = (float) flyoverController.getViewAngle();
                if (!elevationDataExist || !ElevationHelper.getInstance().tilesExist()) {
                    currentXPos = (float) -pos.x;
                    currentYPos = (float) -pos.y;
                    currentZ = (float) (flyoverController.getZoom());
                } else {
                    if (flyoverController.getAnimationState() == FlyoverController.AnimationState.TiltGreencenter || flyoverController.getAnimationState() == FlyoverController.AnimationState.WaitForFinish) {
                        double endViewShift = calculateViewOffsetInPoint(flyoverParameters.endPos.x, flyoverParameters.endPos.y, flyoverController.getViewAngle());
                        double viewShift = flyoverParameters.getStartViewShift() + (endViewShift - flyoverParameters.getStartViewShift()) * flyoverController.getCompletePercentage();
                        currentXPos = (float) (-pos.x + Math.cos(VectorMath.deg2rad((90 + currentRotationAngle))) * viewShift);
                        currentYPos = (float) (-pos.y - Math.sin(VectorMath.deg2rad((90 + currentRotationAngle))) * viewShift);
                        currentZ = (float) (flyoverController.getZoom());
                        double currentViewBone = viewShift / Math.sin(VectorMath.deg2rad((currentViewAngle)));
                        double diff = viewBone - currentViewBone;
                        currentZ = (float) (currentZ + (diff));
                    } else {
                        double endViewShift = calculateViewOffsetInPoint(flyoverParameters.endPos.x, flyoverParameters.endPos.y, flyoverController.getViewAngle());
                        double viewShift = flyoverParameters.getStartViewShift() + (endViewShift - flyoverParameters.getStartViewShift()) * flyoverController.getCompletePercentage();
                        currentXPos = (float) (-pos.x + Math.cos(VectorMath.deg2rad((90 + currentRotationAngle))) * viewShift);
                        currentYPos = (float) (-pos.y - Math.sin(VectorMath.deg2rad((90 + currentRotationAngle))) * viewShift);
                        currentZ = (float) (flyoverController.getZoom());
                        viewBone = viewShift / Math.sin(VectorMath.deg2rad((currentViewAngle)));
                    }
                }


            } else if (previousNavigationMode == NavigationMode.FlyoverPause && navigationMode == NavigationMode.Flyover) {
                flyoverController.resume();
            } else if (navigationMode == NavigationMode.Flyover) {
                needToApplyFlyoverMode = true;
            } else if (navigationMode == NavigationMode.GreenView) {
                needToApplyGreenView = true;
            } else if (navigationMode == NavigationMode.FreeCam) {
                needToApplyFreeCamMode = true;
            } else if (isOverallMode()) {
                needToApplyOverallHoleMode = true;
            } else if (navigationMode == NavigationMode.NavigationMode2D) {
                needToApplyHole2DView = true;
            }

        }

        if (hazardMarkersToDestroy != null) {
            for (final DistanceMarker marker : hazardMarkersToDestroy) {
                marker.destroy();
            }
            hazardMarkersToDestroy = null;
        }

        if (needToInitializeHazardMarkers) {
            needToInitializeHazardMarkers = false;
            if (hazardMarkerArray != null)
                for (DistanceMarker aHazardMarkerArray : hazardMarkerArray) {
                    aHazardMarkerArray.initialize(context, typeface);
                }
        }

        if (frontGreenMarker != null && !frontGreenMarker.isInitialized()) {
            frontGreenMarker.initialize(context, R.drawable.v3d_frontgreen_distance_target, typeface);
        }

        if (backGreenMarker != null && !backGreenMarker.isInitialized()) {
            backGreenMarker.initialize(context, R.drawable.v3d_backgreen_distance_target, typeface);
        }

        if (tapDistanceMarker != null && !tapDistanceMarker.isInitialized()) {
            tapDistanceMarker.initialize(context, R.drawable.v3d_custom_distance_target, typeface);
        }

        if (needToApplyFlyoverMode) {
            needToApplyFlyoverMode = false;
            flyoverController = new FlyoverController();
            flyoverController.setCentralpath(centralpath, green);
            flyoverController.setDefaultViewAngle(FLYOVER_VIEW_ANGLE);
            flyoverController.setDefaultZoom(flyoverParameters.getDefaultZoom());
            flyoverController.setEndZoom(flyoverParameters.getEndZoom());
            flyoverController.setFlyingSpeed(elevationDataExist && ElevationHelper.getInstance().tilesExist() ? 1.5 : 1.7);
            elevationFlyoverEndPosOffset = calculateViewOffset();
            viewShift = calculateViewOffset();
            flyoverController.start();

            if (navigationModeChangedListener != null) {
                navigationModeChangedListener.onNavigationModeChanged(this.navigationMode);
            }
        }

        if (needToUpdateDistances) {
            needToUpdateDistances = false;
            updateDistanceMarkers();
        }

        if (needToApplyGreenView) {

            currentRotation = 0;
            if (centralpath != null && centralpath.getFirstPointList() != null && centralpath.getFirstPointList().getPointList() != null && centralpath.getFirstPointList().getPointList() != null && centralpath.getFirstPointList().getPointList().length > 1) {
                try {
                    Vector startPos = centralpath.getFirstPointList().getFirstPoint();
                    Vector endPos = centralpath.getFirstPointList().getLastPoint();
                    currentRotationAngle = calculateRotationAngleWithStartPos(startPos, endPos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            applyGreenView(currentRotationAngle, viewAngle);
            needToApplyGreenView = false;
            needToUpdateLocation = false;
        }

        if (needToApplyFreeCamMode) {
            needToApplyFreeCamMode = false;
            setCurrentLocationGPSInternal(currentLocationGPS, true, true, true);
            currentZ = FREE_CAM_ZPOS;
            currentViewAngle = FREE_CAM_VIEW_ANGLE;

            if (elevationDataExist && ElevationHelper.getInstance().tilesExist()) {
                double alt = ElevationHelper.getInstance().getHeightInPoint(-currentXPos, -currentYPos, (Course3DRenderer) this);
                double shift = Math.abs(alt * Math.tan(VectorMath.deg2rad(currentViewAngle)));
                currentXPos = (float) (currentXPos + Math.cos(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
                currentYPos = (float) (currentYPos - Math.sin(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
                currentZ = (float) (currentZ - (alt / Math.cos(VectorMath.deg2rad(FREE_CAM_VIEW_ANGLE))));

                try {
                    final Integer parValue = parDataMap.get(holeWithinCourse.getCourseId())[holeWithinCourse.getHoleNumber() - 1];
                    float normalizedZ = (float) normalizeZ(Math.min((FREE_CAM_ZPOS - (alt / Math.cos(VectorMath.deg2rad(currentViewAngle)))), flyoverParameters.getDefaultZoom()));
                    //addLog("needToApplyFreeCamMode: " + currentZ + " + currentZ +  " + normalizedZ + " +normalizedZ ");
                    float treshhold = 2;
                    if ((Math.abs(normalizedZ) - Math.abs(currentZ) < treshhold) && parValue == 3)
                        currentZ = normalizedZ;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (navigationModeChangedListener != null) {
                navigationModeChangedListener.onNavigationModeChanged(this.navigationMode);
            }

        }

        x = currentXPos + offsetX;
        y = currentYPos + offsetY;
        z = currentZ * currentZFactor;

        if (navigationMode != NavigationMode.NavigationMode2D)
            rotationAngle = currentRotationAngle + currentRotation;

        float viewAngle = currentViewAngle;


        if (needToApplyOverallHoleMode) {
            needToApplyOverallHoleMode = false;
            needToCalculateGreenPosition = true;

            if (navigationMode == NavigationMode.OverallHole3) {
                applyOverallHole3();
            }

            if (navigationModeChangedListener != null) {
                navigationModeChangedListener.onNavigationModeChanged(this.navigationMode);
            }

            x = currentXPos + offsetX;
            y = currentYPos + offsetY;
            z = currentZ * currentZFactor;

            if (navigationMode != NavigationMode.NavigationMode2D)
                rotationAngle = currentRotationAngle + currentRotation;
            viewAngle = currentViewAngle;
        }

        if (needToApplyHole2DView) {
            needToApplyHole2DView = false;
            applyHole2D(false);
            if (navigationModeChangedListener != null)
                navigationModeChangedListener.onNavigationModeChanged(this.navigationMode);
            x = currentXPos + offsetX;
            y = currentYPos + offsetY;
            z = currentZ * currentZFactor;
        }

        if (needToUpdateLocation) {
            drawLocationMarker = true;
            needToUpdateLocation = false;
            setCurrentLocationGPSInternal(newLocationGps, false, updateCameraPos, false);
        }

        fpsCalculator.frame();

        if (navigationMode == NavigationMode.Flyover || navigationMode == NavigationMode.FlyoverPause) {
            if (navigationMode == NavigationMode.Flyover) {
                flyoverController.tick();
                Vector pos = flyoverController.getPosition();
                rotationAngle = (float) flyoverController.getRotationAngle();
                viewAngle = (float) flyoverController.getViewAngle();
                if (!elevationDataExist || !ElevationHelper.getInstance().tilesExist()) {
                    x = (float) -pos.x;
                    y = (float) -pos.y;
                    z = (float) (flyoverController.getZoom());
                } else {
                    if (flyoverController.getAnimationState() == FlyoverController.AnimationState.TiltGreencenter || flyoverController.getAnimationState() == FlyoverController.AnimationState.WaitForFinish) {
                        double vS = calculateViewOffsetInPoint(flyoverParameters.endPos.x, flyoverParameters.endPos.y, viewAngle);
                        x = (float) (-pos.x + Math.cos(VectorMath.deg2rad((90 + rotationAngle))) * vS);
                        y = (float) (-pos.y - Math.sin(VectorMath.deg2rad((90 + rotationAngle))) * vS);
                        z = (float) (flyoverController.getZoom());
                        double currentViewBone = vS / Math.sin(VectorMath.deg2rad((viewAngle)));
                        double diff = viewBone - currentViewBone;
                        z = (float) (z + (diff));
                    } else {
                        double endViewShift = calculateViewOffsetInPoint(flyoverParameters.endPos.x, flyoverParameters.endPos.y, viewAngle);
                        double vS = flyoverParameters.getStartViewShift() + (endViewShift - flyoverParameters.getStartViewShift()) * flyoverController.getCompletePercentage();
                        x = (float) (-pos.x + Math.cos(VectorMath.deg2rad((90 + rotationAngle))) * vS);
                        y = (float) (-pos.y - Math.sin(VectorMath.deg2rad((90 + rotationAngle))) * vS);
                        z = (float) (flyoverController.getZoom());
                        viewBone = vS / Math.sin(VectorMath.deg2rad((viewAngle)));
                        previousZ = z;
                    }

                }


            }
            if (flyoverController != null && flyoverController.isFinished()) {
                if (previousNavigationMode == NavigationMode.NavigationMode2D) {
                    setNavigationMode(NavigationMode.NavigationMode2D);
                } else {
                    setNavigationMode(DEFAULT_OVERALL_MODE);
                }
                if (flyoverFinishListener != null)
                    flyoverFinishListener.onFlyoverFinished();
                requestRender();
                return;
            }
        } else if (navigationMode == NavigationMode.NavigationMode2D && lastZoomTime != 0) {
            viewAngle = HOLE_2D_VIEW_ANGLE;

            if (callouts.isHasFocus() || gesturePan.x != 0 || gesturePan.y != 0 || gestureMovePan.x != 0 || gestureMovePan.y != 0 || currentXPos != 0 || currentYPos != 0) {
                lastZoomTime = System.currentTimeMillis();
                startZoomTimeHandler();
            }

            final long timeInterval = System.currentTimeMillis() - lastZoomTime;
            if (timeInterval > AUTO_ZOOM_PERIOD)
                applyHole2D(false);
        }

        if (navigationMode != NavigationMode.NavigationMode2D) {
            lastZoomTime = 0;
            stopZoomTimeHandler();
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
        Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
        Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
        Matrix.translateM(modelViewMatrix, 0, x, y, 0);

        Frustum.updateFrustum(modelViewMatrix, projectionMatrix);
        setCurrentTilesByCameraPos();

        Matrix.setIdentityM(intersectionUnprojectionMatrix, 0);
        Matrix.translateM(intersectionUnprojectionMatrix, 0, -x, -y, 0);
        Matrix.rotateM(intersectionUnprojectionMatrix, 0, -rotationAngle, 0, 0, 1);
        Matrix.rotateM(intersectionUnprojectionMatrix, 0, viewAngle, 1, 0, 0);
        Matrix.translateM(intersectionUnprojectionMatrix, 0, 0, 0, -z);

        bindMatrix();


        if (!drawGroundAndHoleData(x, y, z, viewAngle, rotationAngle)){
            //SOME OF TILES DON'T HAVE TEXTURE, WE NEED TO REDRAW THEM
            onDrawFrame(gl);
            return;
        }

        if (tapCalled) {
            renderTap(tapXValue, tapYValue);
            tapCalled = false;
        }

        float distanceMarkersScale = calculateDistanceMarkersScale();
        boolean drawBackGreenMarker = true;
        float markerOverlapThreshold = 0;
        if (parDataMap != null) {
            final Integer parValue = parDataMap.get(holeWithinCourse.getCourseId())[holeWithinCourse.getHoleNumber() - 1];
            if (sParOverlapThresholds.containsKey(parValue)) {
                markerOverlapThreshold = sParOverlapThresholds.get(parValue);
            }
        }

        if (isCalloutsVisible()) {
            boolean frontGreenIsAvailable = false;
            //enableDepth();
            float zShift = elevationDataExist && ElevationHelper.getInstance().tilesExist() ? 0.05f : 0f;
            if (frontGreenMarker != null && frontGreenMarker.getLocationGps() != null && isCalloutsVisible()) {
                frontGreenMarker.setScale(distanceMarkersScale);
                frontGreenMarker.calculateMatrices(x, y, z + zShift, viewAngle, rotationAngle, projectionMatrix);
                frontGreenMarker.drawGroundMarker(this, viewAngle, rotationAngle, x, y, z + zShift);
                frontGreenIsAvailable = true;
            }

            if (backGreenMarker != null && backGreenMarker.getLocationGps() != null && isCalloutsVisible()) {
                backGreenMarker.setScale(distanceMarkersScale);
                backGreenMarker.calculateMatrices(x, y, z + zShift, viewAngle, rotationAngle, projectionMatrix);

                if (frontGreenIsAvailable) {
                    final double distanceBackFront = backGreenMarker.getProjectedPosition().distance(frontGreenMarker.getProjectedPosition());
                    drawBackGreenMarker = distanceBackFront > markerOverlapThreshold;
                }

                if (drawBackGreenMarker) {
                    backGreenMarker.drawGroundMarker(this, viewAngle, rotationAngle, x, y, z + zShift);
                }
            }

            //disableDepth();

            if (tapDistanceMarker != null && tapDistanceMarker.getLocationGps() != null && isCalloutsVisible()) {
                tapDistanceMarker.calculateMatrices(x, y, z, viewAngle, rotationAngle, projectionMatrix);
                final float tapDistanceScale = navigationMode == NavigationMode.GreenView ? 0.8f : 1.0f;

                tapDistanceMarker.scaleByPosition(tapDistanceScale);
                tapDistanceMarker.drawGroundMarker(this, viewAngle, rotationAngle, x, y, z);
            }
        }

        if (treeArray != null)
            for (final Tree treeObject : treeArray) {
                treeObject.calculateMatrices(x, y, z, viewAngle, rotationAngle, projectionMatrix);
            }
        QuickSort.sortInplace(treeArray, Tree.comparator);
        if (!drawShadowsToTiles || (!elevationDataExist && !ElevationHelper.getInstance().tilesExist())) {
            if (navigationMode != NavigationMode.NavigationMode2D && treeArray != null && treeArray.length < 70)
                for (Tree aTreeArray : treeArray) {
                    aTreeArray.drawShadow(this, x, y, z, rotationAngle, viewAngle);
                }
        }

        final float flagScale = navigationMode == NavigationMode.GreenView ? 1.2f : 1.0f;

        if (flagLocation == null)
            flagLocation = getFlagLocation();
        float zFlagLocation = ElevationHelper.getInstance().getHeightInPoint((float) flagLocation.getLongitude(), (float) flagLocation.getLatitude(), this);
        if (navigationMode == NavigationMode.NavigationMode2D) {

            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x + (float) flagLocation.getLongitude(), y + (float) flagLocation.getLatitude(), 0);
            Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
            Matrix.scaleM(modelViewMatrix, 0, 0.25f, 0.25f, 0.25f);
            Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
            bindMatrix();
            flagPolygon.draw(this, DEFAULT_OPACITY);

            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x + (float) flagLocation.getLongitude(), y + (float) flagLocation.getLatitude(), zFlagLocation);
            Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
            Matrix.scaleM(modelViewMatrix, 0, 0.25f, 0.25f, 0.25f);
            Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
            bindMatrix();
            //enableDepth();

            for (Tree aTreeArray : treeArray) {
                aTreeArray.draw(this, x, y, z, rotationAngle, viewAngle);
            }
            //disableDepth();


        } else {
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);//perspective
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x + (float) flagLocation.getLongitude(), y + (float) flagLocation.getLatitude(), zFlagLocation);
            Matrix.rotateM(modelViewMatrix, 0, 90, 1, 0, 0);// rotated to camera
            Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 1, 0);
            Matrix.scaleM(modelViewMatrix, 0, 0.10f * flagScale, 0.10f * flagScale, 0.10f * flagScale);
            Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);

            bindMatrix();
            enableDepth();
            flagPolygon.draw(this, DEFAULT_OPACITY);
            disableDepth();


//            for (Bunker3DPolygon polygon : bunker3DLayer.getBunkerPolygons()) {
//                if (polygon.rawPointListResized != null)
//                    for (Vector vector : polygon.rawPointListResized) {
//
//                        Matrix.setIdentityM(modelViewMatrix, 0);
//                        Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
//                        Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);//perspective
//                        Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
//                        Matrix.translateM(modelViewMatrix, 0, x + (float) vector.x, y + (float) vector.y, ElevationHelper.getInstance().getHeightInPoint(vector, this));
//                        Matrix.rotateM(modelViewMatrix, 0, 90, 1, 0, 0);// rotated to camera
//                        Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 1, 0);
//                        Matrix.scaleM(modelViewMatrix, 0, 0.10f * flagScale, 0.10f * flagScale, 0.10f * flagScale);
//                        Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
//
//                        bindMatrix();
//                        enableDepth();
//                        pinRedPolygon.draw(this, DEFAULT_OPACITY);
//                        disableDepth();
//
//                    }
//
//            }
//
//            for (Bunker3DPolygon polygon : bunker3DLayer.getBunkerPolygons()) {
//                if (polygon.rawPointListResized != null)
//                    for (Vector vector : polygon.rawPointListResized2) {
//
//                        Matrix.setIdentityM(modelViewMatrix, 0);
//                        Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
//                        Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);//perspective
//                        Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
//                        Matrix.translateM(modelViewMatrix, 0, x + (float) vector.x, y + (float) vector.y, ElevationHelper.getInstance().getHeightInPoint(vector, this));
//                        Matrix.rotateM(modelViewMatrix, 0, 90, 1, 0, 0);// rotated to camera
//                        Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 1, 0);
//                        Matrix.scaleM(modelViewMatrix, 0, 0.10f * flagScale, 0.10f * flagScale, 0.10f * flagScale);
//                        Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
//
//                        bindMatrix();
//                        enableDepth();
//                        pinYellowPolygon.draw(this, DEFAULT_OPACITY);
//                        disableDepth();
//
//                    }
//
//            }
//
//
//            for (Bunker3DPolygon polygon : bunker3DLayer.getBunkerPolygons()) {
//                if (polygon.rawPointListResized != null)
//                    for (Vector vector : polygon.rawPointListResized3) {
//
//                        Matrix.setIdentityM(modelViewMatrix, 0);
//                        Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
//                        Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);//perspective
//                        Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
//                        Matrix.translateM(modelViewMatrix, 0, x + (float) vector.x, y + (float) vector.y, ElevationHelper.getInstance().getHeightInPoint(vector, this));
//                        Matrix.rotateM(modelViewMatrix, 0, 90, 1, 0, 0);// rotated to camera
//                        Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 1, 0);
//                        Matrix.scaleM(modelViewMatrix, 0, 0.10f * flagScale, 0.10f * flagScale, 0.10f * flagScale);
//                        Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
//
//                        bindMatrix();
//                        enableDepth();
//                        pinGreenPolygon.draw(this, DEFAULT_OPACITY);
//                        disableDepth();
//
//                    }
//
//            }

//            int i = 0;
//            for (Bunker3DPolygon polygon : bunker3DLayer.getBunkerPolygons()) {
//                if (polygon.resizedPolygons != null)
//                    for (List<Vector> list : polygon.resizedPolygons) {
//                        for (Vector vector : list) {
//                            Matrix.setIdentityM(modelViewMatrix, 0);
//                            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
//                            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);//perspective
//                            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
//                            Matrix.translateM(modelViewMatrix, 0, x + (float) vector.x, y + (float) vector.y, (float) vector.z);
//                            Matrix.rotateM(modelViewMatrix, 0, 90, 1, 0, 0);// rotated to camera
//                            Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 1, 0);
//                            Matrix.scaleM(modelViewMatrix, 0, 0.10f * flagScale, 0.10f * flagScale, 0.10f * flagScale);
//                            Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
//
//                            bindMatrix();
//                            enableDepth();
//                            pinGreenPolygon.draw(this, DEFAULT_OPACITY);
//                            /*if (i == 1)
//                                pinGreenPolygon.draw(this, DEFAULT_OPACITY);
//                            */
//                            disableDepth();
//
//                        }
//                        i++;
//
//                    }
//
//            }

            for (Tree aTreeArray : treeArray) {
                aTreeArray.draw(this, x, y, z, rotationAngle, viewAngle);
            }
        }

        if (isCalloutsVisible()) {
            if (hazardMarkerArray != null)
                for (DistanceMarker aHazardMarkerArray : hazardMarkerArray) {
                    aHazardMarkerArray.calculateMatrices(x, y, z, viewAngle, rotationAngle, projectionMatrix);
                }

            QuickSort.sortInplace(hazardMarkerArray, DistanceMarker.comparator);

            if (hazardMarkerArray != null)
                for (int i = 0; i < hazardMarkerArray.length; i++) {
                    final DistanceMarker marker = hazardMarkerArray[i];

                    double distanceToFront = Float.MAX_VALUE;
                    if (frontGreenMarker != null && frontGreenMarker.getLocationGps() != null) {
                        distanceToFront = frontGreenMarker.getProjectedPosition().distance(marker.getProjectedPosition());
                    }

                    double distanceToBack = Float.MAX_VALUE;
                    if (backGreenMarker != null && backGreenMarker.getLocationGps() != null) {
                        distanceToBack = backGreenMarker.getProjectedPosition().distance(marker.getProjectedPosition());
                    }

                    double distanceToNext = Float.MAX_VALUE;
                    if (i < hazardMarkerArray.length - 1) {
                        distanceToNext = marker.getProjectedPosition().distance(
                                hazardMarkerArray[i + 1].getProjectedPosition()
                        );
                    }

                    marker.setScale(distanceMarkersScale);
                    if (distanceToBack > markerOverlapThreshold &&
                            distanceToFront > markerOverlapThreshold &&
                            distanceToNext > markerOverlapThreshold) {
                        marker.draw(this, viewAngle, rotationAngle, x, y, z, false);
                    }
                }

            if (frontGreenMarker != null && frontGreenMarker.getLocationGps() != null) {
                frontGreenMarker.draw(this, viewAngle, rotationAngle, x, y, z, false);
            }

            if (backGreenMarker != null && backGreenMarker.getLocationGps() != null && drawBackGreenMarker) {
                backGreenMarker.draw(this, viewAngle, rotationAngle, x, y, z, false);
            }

            if (tapDistanceMarker != null && tapDistanceMarker.getLocationGps() != null) {
                tapDistanceMarker.draw(this, viewAngle, rotationAngle, x, y, z, true);
            }
        }

        if (navigationMode != NavigationMode.NavigationMode2D)
            for (int i = 0; i < aheadCartsListGps.size(); i++) {
                final Location location = aheadCartsListGps.get(i);

                final float locationY = (float) Layer.transformLat(location.getLatitude());
                final float locationX = (float) Layer.transformLon(location.getLongitude());

                if (!perimeter.getBoundingBox().contains(locationX, locationY)) {
                    continue;
                }
                float locationZ = ElevationHelper.getInstance().getHeightInPoint(locationX, locationY, this);
                Matrix.setIdentityM(modelViewMatrix, 0);
                Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
                Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);//perspective
                Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
                Matrix.translateM(modelViewMatrix, 0, x + locationX, y + locationY, locationZ);
                Matrix.rotateM(modelViewMatrix, 0, 90, 1, 0, 0); // rotated to camera
                Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 1, 0);
                Matrix.scaleM(modelViewMatrix, 0, 0.15f, 0.15f, 0.15f);
                Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
                bindMatrix();
                enableDepth();
                aheadCartMarker.draw(this, DEFAULT_OPACITY);
                disableDepth();
            }

        // draw current location
        float locationX = (float) currentLocation.getLongitude();
        float locationY = (float) currentLocation.getLatitude();
        float locationZ = ElevationHelper.getInstance().getHeightInPoint(currentLocation.getLongitude(), currentLocation.getLatitude(), this);
        if (drawLocationMarker && cartLocationVisible) {
            if (navigationMode != NavigationMode.NavigationMode2D) {
                Matrix.setIdentityM(modelViewMatrix, 0);
                Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
                Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);//perspective
                Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
                Matrix.translateM(modelViewMatrix, 0, x + locationX, y + locationY, locationZ);
                Matrix.rotateM(modelViewMatrix, 0, 90, 1, 0, 0); // rotated to camera
                Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 1, 0);
                Matrix.scaleM(modelViewMatrix, 0, 0.2f, 0.2f, 0.2f);
                Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
                bindMatrix();
                enableDepth();
                locationMarker.draw(this, DEFAULT_OPACITY);
                disableDepth();
            }
        }

        if (tapDistanceCreateTime != 0) {
            final long diff = System.currentTimeMillis() - tapDistanceCreateTime;
            if (diff > TAP_DISTANCE_DISPLAY_TIME) {
                tapDistanceCreateTime = 0;
                if (tapDistanceMarker != null) {
                    tapDistanceMarker.destroy();
                    stopTapDistanceMarkerHandler();
                    tapDistanceMarker = null;
                }
            }
        }

        final Vector projectedPosition = unprojectLocation(x, y, z, rotationAngle, viewAngle, locationX, locationY);
        currentLocationVisible = Math.abs(projectedPosition.x) < 0.9 && Math.abs(projectedPosition.y) < 0.9;
        bindMatrix();

        final Set<Map.Entry<Integer, CartDrawData.CartMarker>> entrySet = cartDrawData.cartMarkerArray.entrySet();

        float locationScale = Math.abs(0.037f * z);
        for (Map.Entry<Integer, CartDrawData.CartMarker> entry : entrySet) {

            if(entry.getValue().cartMarker == null)
                entry.getValue().init();

            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x + (float) entry.getValue().cartLocation.getLongitude(), y + (float) entry.getValue().cartLocation.getLatitude(), 0);
            Matrix.rotateM(modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
            Matrix.scaleM(modelViewMatrix, 0, locationScale, locationScale, locationScale);
            Matrix.translateM(modelViewMatrix, 0, 0f, 1f, 0f);
            bindMatrix();
            entry.getValue().cartMarker.draw(this, DEFAULT_OPACITY);
        }

        if (callouts != null && drawCallouts)
            callouts.draw(this);

        if (navigationMode == NavigationMode.Flyover) {
            requestRender();
        }

        if (frameRenderListener != null) {
            frameRenderListener.onFinishedRender();
        }
    }

    public void enableDepth() {
        if (!elevationDataExist || !ElevationHelper.getInstance().tilesExist())
            return;
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    }

    public void disableDepth() {
        if (!elevationDataExist || !ElevationHelper.getInstance().tilesExist())
            return;
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthMask(false);
    }

    public boolean drawGroundAndHoleData(float x, float y, float z, float viewAngle, float rotationAngle) {
        if (draw3DBunkers)
           return drawGroundAndHoleDataWith3DBunkers(x, y, z, viewAngle, rotationAngle);
        else
           return drawGroundAndHoleDataWithout3DBunkers(x, y, z, viewAngle, rotationAngle);

    }

    private boolean drawGroundAndHoleDataWithout3DBunkers(float x, float y, float z, float viewAngle, float rotationAngle) {
        boolean allTilesExist = true;
        if (navigationMode != NavigationMode.NavigationMode2D && elevationDataExist) {

            GLES20.glFrontFace(GLES20.GL_CCW);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(true);
            GLES20.glDepthFunc(GLES20.GL_ALWAYS);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();
            Frustum.updateFrustum(modelViewMatrix, projectionMatrix);
            ground[0].drawSimpleGround(this);
            GLES20.glDepthFunc(GLES20.GL_LEQUAL);
            try {
                skyGradient.draw(this);
                skyClouds.draw(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
            glUseProgram(shadowProgramId);
            bindShadowMatrix();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ground[0].getTextureId());
            GLES20.glUniform1i(shadowTexSampler, 0);
            GLES20.glUniform1f(shadowOpacity, 1);
            GLES20.glUniform1f(shadowIsTexture, 1);
            GLES20.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelViewMatrix, 0);
            GLES20.glUniform3f(lightColor, 0.7f, 0.7f, 0.7f);
            GLES20.glUniform1f(lightAmbientIntensity, 0.6f);
            GLES20.glUniform1f(lightDiffuseIntensity, 0.03f);
            GLES20.glUniform3f(lightDirection, 0.0f, 0.0f, -50f);
            GLES20.glUniform1f(SpecularIntensity, 0.02f);
            GLES20.glUniform1f(Shininess, 0.2f);

            for (int i = 0; i < ground.length; i++)
                ground[i].draw(this, i);

            if (needToCalculateGreenPosition) {
                calculateGreenPosition();
                needToCalculateGreenPosition = false;
            }

            allTilesExist = ElevationHelper.getInstance().drawDrawTiles(this);
            GLES20.glFrontFace(GLES20.GL_CCW);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(false);
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            Matrix.setIdentityM(intersectionUnprojectionMatrix, 0);
            Matrix.translateM(intersectionUnprojectionMatrix, 0, -x, -y, 0);
            Matrix.rotateM(intersectionUnprojectionMatrix, 0, -rotationAngle, 0, 0, 1);
            Matrix.rotateM(intersectionUnprojectionMatrix, 0, viewAngle, 1, 0, 0);
            Matrix.translateM(intersectionUnprojectionMatrix, 0, 0, 0, -z);
            glUseProgram(programId);
            bindMatrix();
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();
        } else {
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            if (flagLocation != null)
                Matrix.translateM(modelViewMatrix, 0, x + (float) flagLocation.getLongitude(), y + (float) flagLocation.getLatitude(), 0);
            else
                Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();
            Frustum.updateFrustum(modelViewMatrix, projectionMatrix);

            if (navigationMode != NavigationMode.NavigationMode2D) {
                try {
                    skyGradient.draw(this);
                    skyClouds.draw(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ground[0].drawSimpleGround(this);

            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();

            Frustum.updateFrustum(modelViewMatrix, projectionMatrix);

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(false);
            if (needToCalculateGreenPosition) {
                calculateGreenPosition();
                needToCalculateGreenPosition = false;
            }
            if (layerList != null)
                for (int i = 0; i < layerList.size(); i++) {
                    if (layerList.get(i).getName() != null && layerList.get(i).getName().toLowerCase().equals("bunker3dborder outline"))
                        continue;
                    layerList.get(i).draw(this);
                }
            if (cartPathList != null)
                for (int i = 0; i < cartPathList.size(); i++) {
                    cartPathList.get(i).draw(this);
                }
        }
        return allTilesExist;
    }

    private boolean drawGroundAndHoleDataWith3DBunkers(float x, float y, float z, float viewAngle, float rotationAngle) {
        boolean allTilesExist = true;
        if (navigationMode != NavigationMode.NavigationMode2D && elevationDataExist) {

            GLES20.glFrontFace(GLES20.GL_CCW);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(true);
            GLES20.glDepthFunc(GLES20.GL_ALWAYS);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();
            Frustum.updateFrustum(modelViewMatrix, projectionMatrix);
            ground[0].drawSimpleGround(this);
            GLES20.glDepthFunc(GLES20.GL_LEQUAL);
            try {
                skyGradient.draw(this);
                skyClouds.draw(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
            glUseProgram(shadowProgramId);
            bindShadowMatrix();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, ground[0].getTextureId());
            GLES20.glUniform1i(shadowTexSampler, 0);
            GLES20.glUniform1f(shadowOpacity, 1);
            GLES20.glUniform1f(shadowIsTexture, 1);
            GLES20.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelViewMatrix, 0);
            GLES20.glUniform3f(lightColor, 0.7f, 0.7f, 0.7f);
            GLES20.glUniform1f(lightAmbientIntensity, 0.6f);
            GLES20.glUniform1f(lightDiffuseIntensity, 0.03f);
            GLES20.glUniform3f(lightDirection, 0.0f, 0.0f, -50f);
            GLES20.glUniform1f(SpecularIntensity, 0.02f);
            GLES20.glUniform1f(Shininess, 0.2f);

            GLES20.glStencilMask(0xFF);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_STENCIL_BUFFER_BIT);
            GLES20.glFrontFace(GLES20.GL_CCW);  // depends on your geometry "GL_CCW" or "GL_CW"
            GLES20.glDisable(GLES20.GL_CULL_FACE);

            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthFunc(GLES20.GL_LESS); // default

            GLES20.glColorMask(true, true, true, true);

            GLES20.glEnable(GLES20.GL_STENCIL_TEST);

            GLES20.glStencilFuncSeparate(GLES20.GL_FRONT, GLES20.GL_ALWAYS, 1, 255);
            GLES20.glStencilFuncSeparate(GLES20.GL_BACK, GLES20.GL_ALWAYS, 1, 255);
            GLES20.glStencilOpSeparate(GLES20.GL_FRONT, GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_REPLACE);
            // WORKING!!!! GLES20.glStencilOpSeparate(GLES20.GL_BACK, GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);
            GLES20.glStencilOpSeparate(GLES20.GL_BACK, GLES20.GL_ZERO, GLES20.GL_KEEP, GLES20.GL_ZERO);


            GLES20.glUniform3f(lightColor, 1f, 1f, 1f);
            GLES20.glUniform1f(lightAmbientIntensity, 0.75f);
            GLES20.glUniform1f(lightDiffuseIntensity, 0.01f);
            GLES20.glUniform1f(SpecularIntensity, 0f);
            GLES20.glUniform1f(Shininess, 0.2f);

            if (bunker3DLayer != null)
                bunker3DLayer.draw(this);

            GLES20.glUniform3f(lightColor, 0.7f, 0.7f, 0.7f);
            GLES20.glUniform1f(lightAmbientIntensity, 0.6f);
            GLES20.glUniform1f(lightDiffuseIntensity, 0.03f);
            GLES20.glUniform1f(SpecularIntensity, 0.02f);
            GLES20.glUniform1f(Shininess, 0.2f);

            GLES20.glStencilFuncSeparate(GLES20.GL_FRONT, GLES20.GL_EQUAL, 0, 255);
            GLES20.glStencilOpSeparate(GLES20.GL_FRONT, GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);
            GLES20.glStencilFuncSeparate(GLES20.GL_BACK, GLES20.GL_ALWAYS, 0, 255);
            GLES20.glStencilOpSeparate(GLES20.GL_BACK, GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_REPLACE);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glCullFace(GLES20.GL_FRONT);


            //
            GLES20.glDepthFunc(GLES20.GL_LEQUAL);
            ground[0].bindTexture(this);
            for (int i = 0; i < ground.length; i++)
                ground[i].draw(this, i);
            GLES20.glDepthMask(false);
            allTilesExist = ElevationHelper.getInstance().drawDrawTiles(this);
            GLES20.glDepthMask(true);
            GLES20.glDepthFunc(GLES20.GL_LESS);
            //


            GLES20.glCullFace(GLES20.GL_BACK);
            GLES20.glColorMask(true, true, true, true);

            //
            GLES20.glDepthFunc(GLES20.GL_LEQUAL);
            ground[0].bindTexture(this);
            for (int i = 0; i < ground.length; i++)
                ground[i].draw(this, i);
            GLES20.glDepthMask(false);
            ElevationHelper.getInstance().drawDrawTiles(this);
            GLES20.glDepthMask(true);
            GLES20.glDepthFunc(GLES20.GL_LESS);
            //

            GLES20.glDisable(GLES20.GL_STENCIL_TEST);

            if (needToCalculateGreenPosition) {
                calculateGreenPosition();
                needToCalculateGreenPosition = false;
            }

            //ElevationHelper.getInstance().drawDrawTiles(this);
            GLES20.glFrontFace(GLES20.GL_CCW);
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(false);
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            Matrix.setIdentityM(intersectionUnprojectionMatrix, 0);
            Matrix.translateM(intersectionUnprojectionMatrix, 0, -x, -y, 0);
            Matrix.rotateM(intersectionUnprojectionMatrix, 0, -rotationAngle, 0, 0, 1);
            Matrix.rotateM(intersectionUnprojectionMatrix, 0, viewAngle, 1, 0, 0);
            Matrix.translateM(intersectionUnprojectionMatrix, 0, 0, 0, -z);
            glUseProgram(programId);
            bindMatrix();
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();
        } else {
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            if (flagLocation != null)
                Matrix.translateM(modelViewMatrix, 0, x + (float) flagLocation.getLongitude(), y + (float) flagLocation.getLatitude(), 0);
            else
                Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();
            Frustum.updateFrustum(modelViewMatrix, projectionMatrix);

            if (navigationMode != NavigationMode.NavigationMode2D) {
                try {
                    skyGradient.draw(this);
                    skyClouds.draw(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ground[0].drawSimpleGround(this);

            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x, y, 0);
            bindMatrix();

            Frustum.updateFrustum(modelViewMatrix, projectionMatrix);

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDepthMask(false);
            if (needToCalculateGreenPosition) {
                calculateGreenPosition();
                needToCalculateGreenPosition = false;
            }
            if (layerList != null)
                for (int i = 0; i < layerList.size(); i++) {

                    if (layerList.get(i).getName() != null && layerList.get(i).getName().toLowerCase().equals("bunker3dborder outline"))
                        continue;

                    layerList.get(i).draw(this);
                }
            if (cartPathList != null)
                for (int i = 0; i < cartPathList.size(); i++) {
                    cartPathList.get(i).draw(this);
                }
        }
        return allTilesExist;
    }

    public Vector[] unprojectViewport() {

        Vector[] retval = new Vector[4];
        retval[0] = unprojectWithTouchPoint(0, 0);
        retval[1] = unprojectWithTouchPoint(viewport.x, 0);
        retval[2] = unprojectWithTouchPoint(viewport.x, viewport.y);
        retval[3] = unprojectWithTouchPoint(0, viewport.y);

        return retval;
    }

    public void tap(float x, float y) {
        tapCalled = true;
        tapXValue = x;
        tapYValue = y;
    }

    private void renderTap(float x,
                           float y) {

        final Ray ray = new Ray(viewport.x, viewport.y, x, y, modelViewMatrix, projectionMatrix);
        if (tapDistanceMarker == null) {
            tapDistanceMarker = new DistanceMarker();
        }
        final float[][] surface = new float[][]{
                new float[]{-5000, 5000, 0}, // top left
                new float[]{-5000, -5000, 0}, // bottom left
                new float[]{5000, -5000, 0}, // bottom right
                new float[]{-5000, 5000, 0}, // top left
                new float[]{5000, -5000, 0}, // bottom right
                new float[]{5000, 5000, 0} // top right
        };

        float[] resultVector = new float[4];
        float[] inputVector = new float[4];

        for (int i = 0; i < surface.length; i++) {
            inputVector[0] = surface[i][0];
            inputVector[1] = surface[i][1];
            inputVector[2] = surface[i][2];
            inputVector[3] = 1;
            Matrix.multiplyMV(resultVector, 0, modelViewMatrix, 0, inputVector, 0);

            surface[i][0] = resultVector[0] / resultVector[3];
            surface[i][1] = resultVector[1] / resultVector[3];
            surface[i][2] = resultVector[2] / resultVector[3];
        }

        Triangle[] surfaceTriangle = new Triangle[]{
                new Triangle(surface[0], surface[1], surface[2]),
                new Triangle(surface[3], surface[4], surface[5])
        };


        float[] intersectionPoint = new float[]{0, 0, 0, 1};

        for (int i = 0; i < surfaceTriangle.length; i++) {
            final int result = Triangle.intersectRayAndTriangle(ray, surfaceTriangle[i], intersectionPoint);
            if (result == 1) {


                if (elevationDataExist && ElevationHelper.getInstance().tilesExist()) {
                    final float[] unprojected = new float[4];
                    Matrix.multiplyMV(unprojected, 0, intersectionUnprojectionMatrix, 0, intersectionPoint, 0);
                    float xpos, ypos;
                    xpos = (float) (unprojected[0] + Math.cos(VectorMath.deg2rad((90 + currentRotationAngle))) * calculateMaxViewOffset() * 2);
                    ypos = (float) (unprojected[1] - Math.sin(VectorMath.deg2rad((90 + currentRotationAngle))) * calculateMaxViewOffset() * 2);

                    tapWithElevations(x, y, ElevationHelper.getInstance().getLeftBottomTapPosition(unprojected[0], unprojected[1], xpos, ypos, this));
                    break;
                } else {

                    final float[] unprojected = new float[4];
                    Matrix.multiplyMV(unprojected, 0, intersectionUnprojectionMatrix, 0, intersectionPoint, 0);
                    final Location location = new Location("");
                    location.setLongitude(Layer.transformToLon(unprojected[0]));
                    location.setLatitude(Layer.transformToLat(unprojected[1]));
                    tapDistanceMarker.setLocationGps(location);
                    if (currentLocationGPS != null) {
                        tapDistanceMarker.setDistance(distance(currentLocationGPS, tapDistanceMarker.getLocationGps()));
                        tapDistanceMarker.recalculatePosition();
                    } else {
                        needToUpdateDistances = true;
                    }
                    break;
                }
            }
        }

        startTapDistanceMarkerHandler();
        tapDistanceCreateTime = System.currentTimeMillis();
        requestRender();

    }

    private float tapXValue;
    private float tapYValue;

    private void tapWithElevations(float x, float y, int[] extremePoints) {

        if (extremePoints[0] == extremePoints[1] && extremePoints[2] == extremePoints[3] && extremePoints[0] == extremePoints[2])
            return;

        final Ray ray = new Ray(viewport.x, viewport.y, x, y, modelViewMatrix, projectionMatrix);

        if (tapDistanceMarker == null) {
            tapDistanceMarker = new DistanceMarker();
        }

        ArrayList<Vector> vectorList = new ArrayList<>();

        for (int y1 = extremePoints[0]; y1 < extremePoints[1]; y1++) {
            for (int x1 = extremePoints[2]; x1 < extremePoints[3]; x1++) {

                Vector leftTop = ElevationHelper.getInstance().getVectorList().get(y1).get(x1);
                Vector bottomLeft = ElevationHelper.getInstance().getVectorList().get(y1 + 1).get(x1);
                Vector bottomRight = ElevationHelper.getInstance().getVectorList().get(y1 + 1).get(x1 + 1);
                Vector topRight = ElevationHelper.getInstance().getVectorList().get(y1).get(x1 + 1);

                final float[][] surface = new float[][]{
                        new float[]{(float) leftTop.x, (float) leftTop.y, (float) leftTop.z}, // top left
                        new float[]{(float) bottomLeft.x, (float) bottomLeft.y, (float) bottomLeft.z}, // bottom left
                        new float[]{(float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z}, // bottom right
                        new float[]{(float) leftTop.x, (float) leftTop.y, (float) leftTop.z}, // top left
                        new float[]{(float) bottomRight.x, (float) bottomRight.y, (float) bottomRight.z}, // bottom right
                        new float[]{(float) topRight.x, (float) topRight.y, (float) topRight.z} // top right
                };

                float[] resultVector = new float[4];
                float[] inputVector = new float[4];


                float[] retValMatrix = new float[16];
                Matrix.setIdentityM(retValMatrix, 0);
                Matrix.translateM(retValMatrix, 0, 0, 0, this.z);
                Matrix.rotateM(retValMatrix, 0, -viewAngle, 1, 0, 0);
                Matrix.rotateM(retValMatrix, 0, rotationAngle, 0, 0, 1);
                Matrix.translateM(retValMatrix, 0, this.x, this.y, 0);

                for (int i = 0; i < surface.length; i++) {
                    inputVector[0] = surface[i][0];
                    inputVector[1] = surface[i][1];
                    inputVector[2] = surface[i][2];
                    inputVector[3] = 1;
                    Matrix.multiplyMV(resultVector, 0, modelViewMatrix, 0, inputVector, 0);

                    surface[i][0] = resultVector[0] / resultVector[3];
                    surface[i][1] = resultVector[1] / resultVector[3];
                    surface[i][2] = resultVector[2] / resultVector[3];
                }

                Triangle[] surfaceTriangle = new Triangle[]{
                        new Triangle(surface[0], surface[1], surface[2]),
                        new Triangle(surface[3], surface[4], surface[5])
                };

                float[] intersectionPoint = new float[]{0, 0, 0, 1};

                for (int i = 0; i < surfaceTriangle.length; i++) {
                    final int result = Triangle.intersectRayAndTriangle(ray, surfaceTriangle[i], intersectionPoint);
                    if (result == 1) {
                        final float[] unprojected = new float[4];
                        Matrix.multiplyMV(unprojected, 0, intersectionUnprojectionMatrix, 0, intersectionPoint, 0);
                        if (!Float.isNaN(unprojected[0]) && !Float.isNaN(unprojected[1])) {
                            vectorList.add(new Vector(unprojected[0], unprojected[1]));
                        }
                        //break;
                    }
                }
            }
        }


        if (vectorList.size() > 0) {
            Vector cameraVector = getCameraProjectionPoint();
            Vector nearest = vectorList.get(0);
            double minDistance = Math.abs(VectorMath.distance(cameraVector, nearest));

            for (Vector retVal : vectorList) {
                double distance = Math.abs(VectorMath.distance(cameraVector, retVal));
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = retVal;
                }
            }
            final Location location = new Location("");
            location.setLongitude(Layer.transformToLon(nearest.x));
            location.setLatitude(Layer.transformToLat(nearest.y));
            if (tapDistanceMarker != null)
                tapDistanceMarker.setLocationGps(location);

            if (currentLocationGPS != null && tapDistanceMarker != null) {
                tapDistanceMarker.setDistance(distance(currentLocationGPS, tapDistanceMarker.getLocationGps()));
                tapDistanceMarker.recalculatePosition();
            } else {
                needToUpdateDistances = true;
            }
        }
    }

    private void createAndUseProgram() {

        int vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.no_shadow_vertex_shader);
        int fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.no_shadow_fragment_shader);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);

        bindSimpleShaders();
        int vertexShadowShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader);
        int fragmentShadowShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader);
        shadowProgramId = ShaderUtils.createProgram(vertexShadowShaderId, fragmentShadowShaderId);
        bindShadowShaders();

        glUseProgram(programId);
    }

    public int modelViewMatrixLocation;
    public int lightColor;
    public int lightAmbientIntensity;
    public int lightDiffuseIntensity;
    public int lightDirection;
    public int SpecularIntensity;
    public int Shininess;
    public int normalLocation;
    public int shadowPositionLocation;
    public int shadowMatrixLocation;
    public int shadowTexCoord, shadowTexSampler, shadowOpacity;
    public int shadowIsTexture, shadowVColor;


    private void bindSimpleShaders() {

        uMatrixLocation = GLES20.glGetUniformLocation(programId, "u_Matrix");
        aPositionLocation = GLES20.glGetAttribLocation(programId, "vPosition");
        vTexCoord = GLES20.glGetAttribLocation(programId, "vTexCoord");
        texSampler = GLES20.glGetUniformLocation(programId, "texSampler");
        opacity = GLES20.glGetUniformLocation(programId, "Opacity");
        isTexture = GLES20.glGetUniformLocation(programId, "isTexture");
        vColor = GLES20.glGetUniformLocation(programId, "uColor");
    }

    public void bindShadowShaders() {

        shadowMatrixLocation = GLES20.glGetUniformLocation(shadowProgramId, "u_Matrix");
        shadowPositionLocation = GLES20.glGetAttribLocation(shadowProgramId, "vPosition");
        shadowTexCoord = GLES20.glGetAttribLocation(shadowProgramId, "vTexCoord");
        shadowTexSampler = GLES20.glGetUniformLocation(shadowProgramId, "texSampler");
        shadowOpacity = GLES20.glGetUniformLocation(shadowProgramId, "Opacity");
        shadowIsTexture = GLES20.glGetUniformLocation(shadowProgramId, "isTexture");
        shadowVColor = GLES20.glGetUniformLocation(shadowProgramId, "uColor");

        normalLocation = GLES20.glGetAttribLocation(shadowProgramId, "vNormal");
        modelViewMatrixLocation = GLES20.glGetUniformLocation(shadowProgramId, "uMVMatrix");
        lightColor = GLES20.glGetUniformLocation(shadowProgramId, "u_Light.Color");
        lightAmbientIntensity = GLES20.glGetUniformLocation(shadowProgramId, "u_Light.AmbientIntensity");
        lightDiffuseIntensity = GLES20.glGetUniformLocation(shadowProgramId, "u_Light.DiffuseIntensity");
        lightDirection = GLES20.glGetUniformLocation(shadowProgramId, "u_Light.Direction");
        SpecularIntensity = GLES20.glGetUniformLocation(shadowProgramId, "u_Light.SpecularIntensity");
        Shininess = GLES20.glGetUniformLocation(shadowProgramId, "u_Light.Shininess");

    }

    public void bindMatrix() {
        if (resultMatrix == null) {
            resultMatrix = new float[16];
        }
        if (projectionMatrix == null) {
            projectionMatrix = new float[16];
            initProjectionMatrix();
        }
        if (modelViewMatrix == null) {
            modelViewMatrix = new float[16];
            requestRender();
            return;
        }

        Matrix.multiplyMM(resultMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, resultMatrix, 0);
    }

    public void bindShadowMatrix() {
        Matrix.multiplyMM(resultMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(shadowMatrixLocation, 1, false, resultMatrix, 0);
    }

    private double calculateGreenViewWithExtraZ(double extraZ, FlyoverController controller) {

        Vector pos = centralpath.getFirstPointList().getLastPoint();
        float greenViewAngle = FLYOVER_VIEW_ANGLE - 10;
        float greenRotationAngle = (float) controller.getRotationAngle();
        float greenX;
        float greenY;
        float greenZ;

        double endAlt = ElevationHelper.getInstance().getHeightInPoint(pos.x, pos.y, this);
        double altZoom = endAlt / Math.cos(VectorMath.deg2rad(70));
        double vS = calculateViewOffsetInPoint(pos.x, pos.y, greenViewAngle);

        double retVal = (FLYOVER_ZPOS * 0.68 - altZoom) - extraZ;
        greenX = (float) (-pos.x + Math.cos(VectorMath.deg2rad((90 + greenRotationAngle))) * vS);
        greenY = (float) (-pos.y - Math.sin(VectorMath.deg2rad((90 + greenRotationAngle))) * vS);


        for (greenZ = 0; greenZ > -200; greenZ = greenZ - 0.1f) {
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, greenZ);
            Matrix.rotateM(modelViewMatrix, 0, -greenViewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, greenRotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, greenX, greenY, 0);
            Frustum.updateFrustum(this);
            if (Frustum.isLayerInFrustum(green, this)) {
                retVal = greenZ - extraZ;
                break;
            }
        }
        return retVal - 0.33;
    }

    private void prepareFlyoverParameters() {

        if (elevationDataExist && ElevationHelper.getInstance().tilesExist()) {

            double maxAlt = 0;

            FlyoverController testFlyoverController = new FlyoverController();

            testFlyoverController.setCentralpath(centralpath, green);
            testFlyoverController.setDefaultViewAngle(FLYOVER_VIEW_ANGLE);
            testFlyoverController.setDefaultZoom(FLYOVER_ZPOS);
            testFlyoverController.setEndZoom(0);
            testFlyoverController.start();

            Vector startPos = centralpath.getFirstPointList().getFirstPoint();

            while (!testFlyoverController.isFinished()) {
                testFlyoverController.testTick();
                Vector pos = testFlyoverController.getPosition();
                double alt = ElevationHelper.getInstance().getHeightInPoint(pos.x, pos.y, this);
                maxAlt = Math.max(maxAlt, alt);
            }

            Vector pos = centralpath.getFirstPointList().getLastPoint();
            double startVs = calculateViewOffsetInPoint(pos.x, pos.y, FLYOVER_VIEW_ANGLE);
            double endVs = calculateViewOffsetInPoint(pos.x, pos.y, FLYOVER_VIEW_ANGLE - 10);
            double extraStartBone = startVs / Math.sin(VectorMath.deg2rad(FLYOVER_VIEW_ANGLE));
            double extraEndBone = endVs / Math.sin(VectorMath.deg2rad(FLYOVER_VIEW_ANGLE - 10));
            double diff = Math.abs(extraStartBone - extraEndBone);
            Vector endPosition = new Vector(testFlyoverController.getPosition());
            double endAlt = ElevationHelper.getInstance().getHeightInPoint(endPosition.x, endPosition.y, this);
            double altZoom = endAlt / Math.cos(VectorMath.deg2rad(70));
            double endZoom = calculateGreenViewWithExtraZ(diff, testFlyoverController);
            double defaultZoom = FLYOVER_ZPOS - (maxAlt / Math.cos(VectorMath.deg2rad(FLYOVER_VIEW_ANGLE)));
            if (Math.abs(defaultZoom - endZoom) < 0.5)
                endZoom = defaultZoom;

            flyoverParameters = new FlyoverParameters(defaultZoom,
                    calculateViewOffsetInPoint(startPos.x, startPos.y, FLYOVER_VIEW_ANGLE),
                    calculateViewOffsetInPoint(endPosition.x, endPosition.y, FLYOVER_VIEW_ANGLE),
                    endZoom,
                    1,
                    (maxAlt / Math.cos(VectorMath.deg2rad(FLYOVER_VIEW_ANGLE))),
                    startPos,
                    endPosition);
        } else {
            flyoverParameters = new FlyoverParameters((FLYOVER_ZPOS),
                    0,
                    0,
                    (FLYOVER_ZPOS * 0.68),
                    1,
                    0,
                    null,
                    null);
        }
    }

    protected boolean isGreenViewVisible() {
        return Frustum.isPointInFrustum(eGB) && Frustum.isPointInFrustum(eGL) && Frustum.isPointInFrustum(eGR) && Frustum.isPointInFrustum(eGT);
    }


}
