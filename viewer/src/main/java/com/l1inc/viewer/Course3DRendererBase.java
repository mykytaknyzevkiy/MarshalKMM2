package com.l1inc.viewer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.ETC1Util;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Looper;
import android.support.annotation.Keep;
import android.util.Log;

import com.google.gson.Gson;
import com.l1inc.viewer.common.Viewer;
//import com.l1inc.viewer.drawing.Bunker3DLayer;
import com.l1inc.viewer.drawing.Callouts;
import com.l1inc.viewer.drawing.CartDrawData;
import com.l1inc.viewer.drawing.CartPath;
import com.l1inc.viewer.drawing.Constants;
import com.l1inc.viewer.drawing.Creek;
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
import com.l1inc.viewer.elevation.TileStaticData;
import com.l1inc.viewer.logging.ViewerLogger;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Path;
import com.l1inc.viewer.math.Ray;
import com.l1inc.viewer.math.Triangle;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;
import com.l1inc.viewer.parcer.BaseShapeObject;
import com.l1inc.viewer.parcer.CourseGpsDetailsData;
import com.l1inc.viewer.parcer.CourseGpsDetailsResponse;
import com.l1inc.viewer.parcer.Hole;
import com.l1inc.viewer.parcer.VectorDataObject;
import com.l1inc.viewer.textureset.DefaultTextureSet;
import com.l1inc.viewer.textureset.DesertTextureSet;
import com.l1inc.viewer.textureset.TextureSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kirill Kartukov on 13.10.2017.
 */

public class Course3DRendererBase {

    public static void setViewerLogger(final ViewerLogger viewerLogger) {
        sViewerLogger = viewerLogger;
    }

    public static ViewerLogger getViewerLogger() {
        return sViewerLogger;
    }

    private static ViewerLogger sViewerLogger;

    protected ShaderUtils.GLProgram programId;
    protected ShaderUtils.GLProgram shadowProgramId;
    public int aPositionLocation;
    public int uMatrixLocation;
    public int vTexCoord, texSampler, opacity;
    public int isTexture, vColor;

    protected Callouts.CalloutsDrawMode calloutsDrawMode;
    protected boolean calloutsShowOverlay = true;

    public final static String ELEVATION = "ELEVATION";
    public final static String GPS_DETAILS = "GPS_DETAILS";
    public final static String COURSE_ID = "COURSE_ID";

    private String fontLocation = "fonts/BebasNeue.ttf";
    private String courseId = "";

    private CourseGpsDetailsResponse courseGpsDetailsResponse;

    private ArrayList<Double> zNormalizeArray = new ArrayList<>();

    @Keep
    public enum NavigationMode {
        FreeCam,
        Flyover,
        FlyoverPause,
        GreenView,
        OverallHole3,
        NavigationMode2D
    }


    // Our vertices.
    private float vertices[] = {
            -1.0f, 1.0f, 0.0f,  // 0, Top Left
            -1.0f, -1.0f, 0.0f,  // 1, Bottom Left
            1.0f, -1.0f, 0.0f,  // 2, Bottom Right
            1.0f, 1.0f, 0.0f,  // 3, Top Right
    };


    //IndexedTextureSquare DATA
    // The order we like to connect them.
    public final short[] indices = {0, 1, 2, 0, 2, 3};

    public final float textureCoordinates[] = {0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f};

    private float vertices3D[] = {
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,

            0.0f, 1.0f, -1.0f,
            0.0f, -1.0f, -1.0f,
            0.0f, -1.0f, 1.0f,
            0.0f, 1.0f, -1.0f,
            0.0f, -1.0f, 1.0f,
            0.0f, 1.0f, 1.0f

    };

    public final float textureCoordinates3d[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

    };

    // Our vertex buffer.
    public FloatBuffer vertexBuffer;

    // Our index buffer.
    public ShortBuffer indexBuffer;

    // Our vertex buffer.
    public FloatBuffer textureBuffer;

    // Our vertex buffer.
    public FloatBuffer vertexBufferTree;

    // Our vertex buffer.
    public FloatBuffer textureBufferTree;

    public FloatBuffer vertexBuffer2DGround;
    public FloatBuffer textureBuffer2DGround;

    List<Float> vertexList2DGround = new ArrayList<>();
    List<Float> uvList2DGround = new ArrayList<>();
    private int vertexCount2D;

    protected boolean drawShadowsToTiles = true;
    protected boolean tapCalled = false;
    protected boolean draw3DBunkers = false;
    protected boolean needRedrawTiles = false;

    protected enum TextureSetType {
        DEFAULT,
        TROPICAL,
        CUSTOM
    }

    protected TextureSetType textureSetType = TextureSetType.DEFAULT;

    Location frontLocation = null;
    Location backLocation = null;
    List<Location> hazardList = null;

    private String cartFontLocation = "fonts/arial_narrow_bold.ttf";

    protected CartDrawData cartDrawData;

    private void createBuffers(float tiles) {
        textureCoordinates[3] = textureCoordinates[4] = textureCoordinates[5] = textureCoordinates[6] = tiles;
        // a float is 4 bytes, therefore we multiplied the number if
        // vertices with 4.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // short is 2 bytes, therefore we multiplied the number if
        // vertices with 2.
        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        textureBuffer.put(textureCoordinates);
        textureBuffer.position(0);
        createTreeBuffers();
    }

    private void createTreeBuffers() {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices3D.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBufferTree = vbb.asFloatBuffer();
        vertexBufferTree.put(vertices3D);
        vertexBufferTree.position(0);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCoordinates3d.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBufferTree = byteBuf.asFloatBuffer();
        textureBufferTree.put(textureCoordinates3d);
        textureBufferTree.position(0);
    }

    protected void bind2DGround() {

        float xC2;
        float yC2;
        float zC2;

        double currentAngle = 0;

        vertexList2DGround.clear();
        uvList2DGround.clear();

        while (currentAngle < Math.PI * 2) {
            xC2 = (float) (Math.cos(currentAngle) * Constants.SCENE_RADIUS);
            yC2 = (float) (Math.sin(currentAngle) * Constants.SCENE_RADIUS);
            zC2 = 0f;
            vertexList2DGround.add(xC2);
            vertexList2DGround.add(yC2);
            vertexList2DGround.add(zC2);
            uvList2DGround.add(xC2);
            uvList2DGround.add(yC2);
            currentAngle += VectorMath.deg2rad(Constants.SCENE_ANGLE_STEP);
        }

        vertexCount2D = vertexList2DGround.size();

        ByteBuffer vbb2 = ByteBuffer.allocateDirect(vertexCount2D * 4);
        vbb2.order(ByteOrder.nativeOrder());
        vertexBuffer2DGround = vbb2.asFloatBuffer();
        for (Float val : vertexList2DGround) {
            vertexBuffer2DGround.put(val);
        }

        vertexBuffer2DGround.position(0);


        ByteBuffer byteBuf2 = ByteBuffer.allocateDirect(uvList2DGround.size() * 4);
        byteBuf2.order(ByteOrder.nativeOrder());
        textureBuffer2DGround = byteBuf2.asFloatBuffer();
        for (Float val : uvList2DGround) {
            textureBuffer2DGround.put(val);
        }
        textureBuffer2DGround.position(0);
    }

    public FloatBuffer getVertexBuffer2DGround() {
        return vertexBuffer2DGround;
    }

    public FloatBuffer getTextureBuffer2DGround() {
        return textureBuffer2DGround;
    }

    public int getVertexCount2D() {
        return vertexCount2D;
    }

    protected void bindIndexedTextureSquareBuffers() {
        createBuffers(1);
    }

    protected android.os.Handler lastZoomTimeHandler;
    protected android.os.Handler tapDistanceMarkerHandler;

    protected Runnable lastZoomTimeRunnable = new Runnable() {
        @Override
        public void run() {
            applyHole2D(false);
            requestRender();
        }
    };

    protected Runnable tapDistanceMarkerRunnable = new Runnable() {
        @Override
        public void run() {
            if (tapDistanceMarker != null) {
                tapDistanceMarker.destroy();
                tapDistanceMarker = null;
            }
            requestRender();
        }
    };

    protected void startZoomTimeHandler() {
        if (lastZoomTimeHandler == null)
            lastZoomTimeHandler = new android.os.Handler(Looper.getMainLooper());
        lastZoomTimeHandler.removeCallbacksAndMessages(null);
        lastZoomTimeHandler.removeCallbacks(lastZoomTimeRunnable);
        lastZoomTimeHandler.postDelayed(lastZoomTimeRunnable, AUTO_ZOOM_PERIOD);
    }

    protected void stopZoomTimeHandler() {
        if (lastZoomTimeHandler == null)
            lastZoomTimeHandler = new android.os.Handler(Looper.getMainLooper());
        lastZoomTimeHandler.removeCallbacksAndMessages(null);
        lastZoomTimeHandler.removeCallbacks(lastZoomTimeRunnable);
    }

    protected void startTapDistanceMarkerHandler() {
        if (tapDistanceMarkerHandler == null)
            tapDistanceMarkerHandler = new android.os.Handler(Looper.getMainLooper());
        tapDistanceMarkerHandler.removeCallbacksAndMessages(null);
        tapDistanceMarkerHandler.removeCallbacks(tapDistanceMarkerRunnable);
        tapDistanceMarkerHandler.postDelayed(tapDistanceMarkerRunnable, TAP_DISTANCE_DISPLAY_TIME);
    }

    protected void stopTapDistanceMarkerHandler() {
        if (tapDistanceMarkerHandler == null)
            tapDistanceMarkerHandler = new android.os.Handler(Looper.getMainLooper());
        tapDistanceMarkerHandler.removeCallbacksAndMessages(null);
        tapDistanceMarkerHandler.removeCallbacks(tapDistanceMarkerRunnable);
    }

    public class ProjectionCalculator {
        public final float[] modelViewProjectionMatrix = new float[16];
        float[] vIn = new float[]{0, 0, 0, 1};
        float[] vOut = new float[4];
        Vector retval = new Vector();

        public Vector unprojectLocation(final float x,
                                        final float y,
                                        final float z,
                                        final float rotationAngle,
                                        final float viewAngle,
                                        final float locationX,
                                        final float locationY) {

            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, z);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, x + locationX, y + locationY, 0);
            Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
            Matrix.multiplyMV(vOut, 0, modelViewProjectionMatrix, 0, vIn, 0);
            //return new Vector(vOut[0] / vOut[3], vOut[1] / vOut[3]);
            retval.x = vOut[0] / vOut[3];
            retval.y = vOut[1] / vOut[3];
            return retval;
        }
    }

    protected boolean isDetroying = false;
    protected boolean isPreparingData = false;

    public void destroyAllData() {
        if (cartDrawData != null)
            cartDrawData.cartMarkerArray.clear();
        cartDrawData = null;
        isDetroying = true;
        isPreparingData = false;
        if (vertexBuffer != null)
            vertexBuffer.clear();
        if (indexBuffer != null)
            indexBuffer.clear();
        if (textureBuffer != null)
            textureBuffer.clear();
        if (vertexBufferTree != null)
            vertexBufferTree.clear();
        if (vertexBuffer2DGround != null)
            vertexBuffer2DGround.clear();
        if (uvList2DGround != null)
            uvList2DGround.clear();
        callouts.destroy();
        //callouts = null;
        if (layerList != null) {
            for (IDrawable layer : layerList) {
                layer.destroy();
                layer = null;
            }

            layerList.clear();
        }

        if (ground != null)
            for (Ground ground : ground)
                ground.destroy();

//        if (bunker3DLayer != null)
//            bunker3DLayer.destroy();

        vectorGPSObject = null;

        if (cartPathList != null) {
            for (CartPath layer : cartPathList) {
                layer.destroy();
                layer = null;
            }
            cartPathList.clear();
        }

        if (courseVectorDataMap != null) {
            courseVectorDataMap.clear();
        }
        viewerClickable = true;
    }

    private void destroyCourseData() {
        if (layerList != null) {
            for (IDrawable layer : layerList) {
                layer.destroy();
                layer = null;
            }

            layerList.clear();
        }

        if (ground != null)
            for (Ground ground : ground)
                ground.destroy();

//        if (bunker3DLayer != null)
//            bunker3DLayer.destroy();

        vectorGPSObject = null;

        if (cartPathList != null) {
            for (CartPath layer : cartPathList) {
                layer.destroy();
                layer = null;
            }
            cartPathList.clear();
        }

        TextureCache.invalidate();
    }


    protected int[] locationOnScreen = new int[2];

    protected Vector extremeGreenTop;
    protected Vector extremeGreenBottom;
    protected Vector extremeGreenLeft;
    protected Vector extremeGreenRight;
    protected Vector eGB;
    protected Vector eGL;
    protected Vector eGR;
    protected Vector eGT;

    public int viewportWidth;
    public int viewportHeight;

    protected CartPath cartPath;
    protected Hole holeObject;

    protected Vector extremeLayersLeft;
    protected Vector extremeLayersRight;
    protected Vector extremeLayersTop;
    protected Vector extremeLayersBottom;

    protected static final float FREE_CAM_ZPOS = -4.0f;
    protected static final float FLYOVER_ZPOS = -3;
    protected static final float MAX_Z = -20;
    protected static final float MAX_Z_2D_VIEW = -50;
    protected static final float MIN_Z_2D_VIEW = -5;
    protected static final long TAP_DISTANCE_DISPLAY_TIME = 7000;
    protected static final long AUTO_ZOOM_PERIOD = 15000;
    protected static final float FREE_CAM_VIEW_ANGLE = viewAngle(30);
    protected static final float FLYOVER_VIEW_ANGLE = viewAngle(20);
    protected static final float GREEN_VIEW_VIEW_ANGLE = viewAngle(40);
    protected static final float OVERALL_HOLE_1_VIEW_ANGLE = viewAngle(40);
    protected static final float OVERALL_HOLE_2_VIEW_ANGLE = viewAngle(50);
    protected static final float OVERALL_HOLE_3_VIEW_ANGLE = viewAngle(30);
    protected static final float HOLE_2D_VIEW_ANGLE = viewAngle(90);

    protected static final float OVERALL_HOLE_1_CENTRALPATH_ADVANCE = 0.25f;
    protected static final float OVERALL_HOLE_2_CENTRALPATH_ADVANCE = 0.35f;
    protected static final float OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FROM_TEEBOX = 0.2f;
    protected static final float OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY = 2.2f;
    protected static final float OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY_PAR4 = 2.7f;
    protected static final float OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY_PAR5 = 3.3f;

    public float[] projectionMatrix = new float[16];
    public float[] resultMatrix = new float[16];

    public float currentZ = FREE_CAM_ZPOS;
    public float greenViewZPos = -2.5f;
    public float currentZFactor = 1;
    public float currentRotationAngle = 0;
    public float currentViewAngle = FREE_CAM_VIEW_ANGLE;
    public float currentRotation = 0;
    public float rotationAngle = 0;
    public float viewAngle = 0;
    public float currentXPos = 0;
    protected float currentXPosVBO = 0;
    protected float currentYPosVBO = 0;
    protected float currentZPosVBO = 0;
    protected Tile tile;
    public float offsetX = 0;
    public float currentYPos = 0;
    public float offsetY = 0;
    public float x = 0;
    public float y = 0;
    public float z = 0;
    public final float DEFAULT_OPACITY = 1;
    public double elevationFlyoverEndPosOffset;

    private Vector moveDirection;
    private Vector cameraPoint;

    public Point gesturePan = new Point(0, 0);
    public Point gestureMovePan = new Point(0, 0);

    protected Layer sandLayer;
    protected Layer sandBorderLayer;
    protected Layer lakeLayer;
    protected Layer lakeBorderLayer;
    protected Layer lavaLayer;
    protected Layer oceanLayer;
    protected Layer oceanBorderLayer;
    protected Layer pondLayer;
    protected Layer pondBorderLayer;
    protected Layer waterLayer;
    protected Layer waterBorderLayer;
    protected Layer bridgeLayer;
    protected TextureSet textureSet;

    protected IndexedTexturedSquare flagPolygon;
    /*protected IndexedTexturedSquare exremeBottom;
    protected IndexedTexturedSquare extremeTop;
    protected IndexedTexturedSquare extremeLeft;
    protected IndexedTexturedSquare extremeRight;*/
    protected Context context;
    protected Course3DViewer glSurfaceView;
    public ArrayList<IDrawable> layerList;
    protected Layer perimeter;
    protected PointListLayer perimeterPointListLayer;
    protected PointListLayer centralpath;
    protected PointListLayer path;
    protected PointListLayer creek;
    protected Tree[] treeArray;
    protected PointListLayer tree;
    protected Layer green;
    protected FPSCalculator fpsCalculator = new FPSCalculator();
    protected HoleWithinCourse holeWithinCourse;
    protected int numHoles;
    protected boolean resetPosition = true;
    protected Location currentLocationGPS;
    protected Location currentLocation;
    protected Location flagLocationGPS;
    protected Location flagLocation;
    protected IndexedTexturedSquare locationMarker;
    protected FloatBuffer distanceLineBuffer;
    protected NavigationMode navigationMode;
    protected NavigationMode newNavigationMode;
    protected NavigationMode previousNavigationMode;
    protected FlyoverController flyoverController;
    protected FlyoverParameters flyoverParameters;
    protected Viewer.GreenPositionChangeListener greenPositionChangeListener;
    protected Viewer.NavigationModeChangedListener navigationModeChangedListener;
    protected Viewer.CurrentHoleChangedListener currentHoleChangedListener;
    protected Viewer.FlyoverFinishListener flyoverFinishListener;
    protected Viewer.CurrentCourseChangedListener currentCourseChangedListener;
    protected Viewer.HoleLoadingStateChangedListener holeLoadingStateChangedListener;
    protected Ground[] ground;
    protected Sky skyGradient;
    protected Sky skyClouds;
    public List<CartPath> cartPathList = new ArrayList<>();
    protected Map<String, String> courseVectorDataMap;
    protected PointListLayer teeboxcenter;
    protected PointListLayer teeboxcenterGPS;
    protected DistanceMarker frontGreenMarker;
    protected DistanceMarker backGreenMarker;
    protected Typeface typeface = Typeface.DEFAULT;
    protected boolean metricUnits = false;
    protected DistanceMarker[] hazardMarkerArray;
    protected List<DistanceMarker> hazardMarkersToDestroy;

    protected boolean needToChangeHole = false;
    protected boolean needToLoadStaticData = false;
    protected boolean needToChangeNavigationMode = false;
    protected boolean needToApplyFlyoverMode;
    protected boolean needToApplyGreenView;
    protected boolean needToLoadCourseData = false;
    protected boolean needToProccedOnSurfaceCreated = false;
    protected boolean needToUpdateDistances = false;
    protected boolean needToInitializeHazardMarkers = false;
    protected boolean needToApplyOverallHoleMode = false;
    protected boolean needToCalculateGreenPosition = false;
    protected boolean needToApplyHole2DView = false;
    protected boolean needToApplyFreeCamMode = false;
    protected boolean needToUpdateLocation = false;
    public boolean elevationDataExist = false;
    protected boolean needToClearGlBuffer = false;

    protected Map<String, Integer[]> parDataMap;
    protected PointListLayer fairway;
    protected Location newLocationGps;
    protected boolean updateCameraPos;
    protected Point viewport;
    public float[] modelViewMatrix = new float[16];
    protected DistanceMarker tapDistanceMarker;
    protected float[] intersectionUnprojectionMatrix = new float[16];
    protected List<Location> aheadCartsListGps = new ArrayList<>();
    protected IndexedTexturedSquare aheadCartMarker;
    protected Map<String, List<PinPositionOverride>> pinPositionOverrideMap = new HashMap<>();
    protected boolean hasPinOverride = false;
    protected PointListLayer greencenter;
    protected long tapDistanceCreateTime = 0;
    protected long lastZoomTime = 0;
    protected boolean currentLocationVisible = false;
    protected boolean drawLocationMarker = false;

    protected ProjectionCalculator projectionCalculator = new ProjectionCalculator();
    protected int initialTeeBox = 0;
    protected boolean cartLocationVisible = true;

    protected Callouts callouts;

    protected Vector finalPosition;

    public double viewShift = 0;

    protected Viewer.FrameRenderListener frameRenderListener;

    public static final NavigationMode DEFAULT_OVERALL_MODE = NavigationMode.OverallHole3;


    protected void clearExtremePoints() {
        extremeLayersLeft = null;
        extremeLayersRight = null;
        extremeLayersTop = null;
        extremeLayersBottom = null;
    }

    protected void updateExtremePoints(Layer layer) {
        if (extremeLayersLeft == null)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight == null)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop == null)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom == null)
            extremeLayersBottom = layer.getExtremeBottom();

        if (extremeLayersLeft.x > layer.getExtremeLeft().x)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight.x < layer.getExtremeRight().x)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop.y < layer.getExtremeTop().y)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom.y > layer.getExtremeBottom().y)
            extremeLayersBottom = layer.getExtremeBottom();
    }

//    protected void updateExtremePoints(Bunker3DLayer layer) {
//        if (extremeLayersLeft == null)
//            extremeLayersLeft = layer.getExtremeLeft();
//        if (extremeLayersRight == null)
//            extremeLayersRight = layer.getExtremeRight();
//        if (extremeLayersTop == null)
//            extremeLayersTop = layer.getExtremeTop();
//        if (extremeLayersBottom == null)
//            extremeLayersBottom = layer.getExtremeBottom();
//
//        if (extremeLayersLeft.x > layer.getExtremeLeft().x)
//            extremeLayersLeft = layer.getExtremeLeft();
//        if (extremeLayersRight.x < layer.getExtremeRight().x)
//            extremeLayersRight = layer.getExtremeRight();
//        if (extremeLayersTop.y < layer.getExtremeTop().y)
//            extremeLayersTop = layer.getExtremeTop();
//        if (extremeLayersBottom.y > layer.getExtremeBottom().y)
//            extremeLayersBottom = layer.getExtremeBottom();
//    }

    protected void updateExtremePoints(CartPath layer) {
        if (extremeLayersLeft == null)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight == null)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop == null)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom == null)
            extremeLayersBottom = layer.getExtremeBottom();

        if (extremeLayersLeft.x > layer.getExtremeLeft().x)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight.x < layer.getExtremeRight().x)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop.y < layer.getExtremeTop().y)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom.y > layer.getExtremeBottom().y)
            extremeLayersBottom = layer.getExtremeBottom();
    }

    protected void updateExtremePoints(OutlineLayer layer) {
        if (extremeLayersLeft == null)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight == null)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop == null)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom == null)
            extremeLayersBottom = layer.getExtremeBottom();

        if (extremeLayersLeft.x > layer.getExtremeLeft().x)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight.x < layer.getExtremeRight().x)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop.y < layer.getExtremeTop().y)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom.y > layer.getExtremeBottom().y)
            extremeLayersBottom = layer.getExtremeBottom();
    }

    protected void updateExtremePoints(Creek layer) {
        if (extremeLayersLeft == null)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight == null)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop == null)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom == null)
            extremeLayersBottom = layer.getExtremeBottom();

        if (extremeLayersLeft.x > layer.getExtremeLeft().x)
            extremeLayersLeft = layer.getExtremeLeft();
        if (extremeLayersRight.x < layer.getExtremeRight().x)
            extremeLayersRight = layer.getExtremeRight();
        if (extremeLayersTop.y < layer.getExtremeTop().y)
            extremeLayersTop = layer.getExtremeTop();
        if (extremeLayersBottom.y > layer.getExtremeBottom().y)
            extremeLayersBottom = layer.getExtremeBottom();
    }

    public synchronized void requestRender() {
        if (glSurfaceView.getRenderMode() == GLSurfaceView.RENDERMODE_WHEN_DIRTY && glSurfaceView != null) {
            glSurfaceView.requestRender();
        }
    }

    protected boolean viewerClickable = true;

    public boolean isViewerClickable() {
        return viewerClickable && !tapCalled;
    }

    protected static class ParViewConfig {
        protected float zoomFairway;
        protected float advanceFairway;
        protected float zoomTeebox;
        protected float advanceTeebox;

        public ParViewConfig(final float zoomFairway,
                             final float advanceFairway,
                             final float zoomTeebox,
                             final float advanceTeebox) {
            this.zoomFairway = zoomFairway;
            this.advanceFairway = advanceFairway;
            this.zoomTeebox = zoomTeebox;
            this.advanceTeebox = advanceTeebox;
        }
    }

    protected static Map<Integer, Float> sParZoomLevels1;
    protected static Map<Integer, Float> sParZoomLevels2;
    protected static Map<Integer, ParViewConfig> sParZoomLevels3;
    protected static Map<Integer, Float> sParOverlapThresholds;
    protected static Map<Integer, ParViewConfig> sParHole2DZoomLevels;

    static {
        sParZoomLevels1 = new HashMap<>();
        sParZoomLevels1.put(0, -12.5f); // default value for unknown par
        sParZoomLevels1.put(3, -6f);
        sParZoomLevels1.put(4, -12.5f);
        sParZoomLevels1.put(5, -16f);
        sParZoomLevels1.put(6, -20f);

        sParZoomLevels2 = new HashMap<>();
        sParZoomLevels2.put(0, -18f); // default value for unknown par
        sParZoomLevels2.put(3, -9f);
        sParZoomLevels2.put(4, -18f);
        sParZoomLevels2.put(5, -21f);
        sParZoomLevels2.put(6, -25f);

        sParZoomLevels3 = new HashMap<>();
        sParZoomLevels3.put(0, new ParViewConfig(
                -5f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY,
                -5f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY
        )); // default value for unknown par

        sParZoomLevels3.put(3, new ParViewConfig(
                -3f, 0, // fairway is ignored for par 3
                -4.0f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FROM_TEEBOX
        ));
        sParZoomLevels3.put(4, new ParViewConfig(
                -6.0f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY_PAR4,
                -5f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY
        ));
        sParZoomLevels3.put(5, new ParViewConfig(
                -7.5f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY_PAR5,
                -7f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY
        ));
        sParZoomLevels3.put(6, new ParViewConfig(
                -7f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY_PAR5,
                -7f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY
        ));

        sParOverlapThresholds = new HashMap<>();
        sParOverlapThresholds.put(0, 0f);
        sParOverlapThresholds.put(3, 0.27f);
        sParOverlapThresholds.put(4, 0.12f);
        sParOverlapThresholds.put(5, 0.1f);
        sParOverlapThresholds.put(6, 0.1f);

        sParHole2DZoomLevels = new HashMap<>();
        sParHole2DZoomLevels.put(0, new ParViewConfig(
                -12f, 3.0f,
                -12f, 0.5f
        )); // default value for unknown par

        sParHole2DZoomLevels.put(3, new ParViewConfig(
                -9.5f, 0.5f,
                -9.5f, 0.5f
        ));
        sParHole2DZoomLevels.put(4, new ParViewConfig(
                -13.5f, 6.5f,
                -13.5f, 0.5f
        ));
        sParHole2DZoomLevels.put(5, new ParViewConfig(
                -17f, 11.0f,
                -17f, 0.5f
        ));
        sParHole2DZoomLevels.put(6, new ParViewConfig(
                -7f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY_PAR5,
                -7f, OVERALL_HOLE_3_CENTRALPATH_ADVANCE_FAIRWAY
        ));
    }

    protected static float viewAngle(final float angle) {
        return 90 - angle;
    }


    public synchronized int getFps() {
        return fpsCalculator.getFps();
    }

    public synchronized HoleWithinCourse getHoleWithinCourse() {
        return holeWithinCourse;
    }

    public int getCurrentHole() {
        int retVal = -1;
        try {
            retVal = holeWithinCourse.getHoleNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public boolean isDraw3DBunkers() {
        return draw3DBunkers;
    }

    public void setDraw3DBunkers(boolean draw3DBunkers) {
        this.draw3DBunkers = draw3DBunkers;
        needRedrawTiles = true;
        requestRender();
    }

    public synchronized NavigationMode getNavigationMode() {
        return navigationMode;
    }

    public synchronized NavigationMode getPendingNavigationMode() {
        return newNavigationMode;
    }

    public synchronized void setNavigationModeChangedListener(final Viewer.NavigationModeChangedListener navigationModeChangedListener) {
        this.navigationModeChangedListener = navigationModeChangedListener;
    }

    public synchronized void setGreenPositionChangeListener(final Viewer.GreenPositionChangeListener greenPositionChangeListener) {
        this.greenPositionChangeListener = greenPositionChangeListener;
    }

    public synchronized void setCurrentCourseChangedListener(final Viewer.CurrentCourseChangedListener currentCourseChangedListener) {
        this.currentCourseChangedListener = currentCourseChangedListener;
    }

    public synchronized void setFlyoverFinishListener(Viewer.FlyoverFinishListener flyoverFinishListener) {
        this.flyoverFinishListener = flyoverFinishListener;
    }

    public synchronized void setCurrentHoleChangedListener(final Viewer.CurrentHoleChangedListener currentHoleChangedListener) {
        this.currentHoleChangedListener = currentHoleChangedListener;
    }

    public synchronized void setHoleLoadingStateChangedListener(final Viewer.HoleLoadingStateChangedListener holeLoadingStateChangedListener) {
        this.holeLoadingStateChangedListener = holeLoadingStateChangedListener;
    }

    public synchronized void setNavigationMode(final NavigationMode navigationMode) {
        if (zNormalizeArray == null)
            zNormalizeArray = new ArrayList<>();
        zNormalizeArray.clear();
        this.newNavigationMode = navigationMode;
        needToChangeNavigationMode = true;
    }

    protected VectorDataObject vectorGPSObject;


    public synchronized void setCourseVectorDataMap(final Map<String, String> courseVectorDataMap, boolean resetElevations) {
        cartDrawData = new CartDrawData(context, Typeface.createFromAsset(context.getAssets(), cartFontLocation));

        isPreparingData = true;
        viewerClickable = true;
        if (context != null)
            typeface = Typeface.createFromAsset(context.getAssets(), fontLocation);
        else
            this.typeface = Typeface.DEFAULT;

        try {
            if (courseVectorDataMap.get(COURSE_ID) != null)
                courseId = courseVectorDataMap.get(COURSE_ID);
            if (courseVectorDataMap.get(GPS_DETAILS) != null)
                courseGpsDetailsResponse = new Gson().fromJson(courseVectorDataMap.get(GPS_DETAILS), CourseGpsDetailsResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //courseGpsDetailsResponse.getGPSList().size();

        this.courseVectorDataMap = courseVectorDataMap;
        if (resetElevations) {
            holeWithinCourse = null;
            destroyCourseData();
            elevationDataExist = false;
            tapCalled = false;
            ElevationHelper.getInstance().reset();
            TileStaticData.getInstance().destroy();
        }
        if (!ElevationHelper.getInstance().isElevationDataLoaded()) {


            tapCalled = false;
            holeWithinCourse = null;
            destroyCourseData();
            elevationDataExist = false;
            ElevationHelper.getInstance().reset();
            TileStaticData.getInstance().destroy();
            //

            Layer.resetBasePoints();
            elevationDataExist = ElevationHelper.getInstance().parseElevationData(courseVectorDataMap.get(ELEVATION), null);
            if (elevationDataExist) {
                //courseVectorDataMap.remove(ELEVATION);
                elevationDataExist = ElevationHelper.getInstance().splitToTiles();
            }
        } else {
            elevationDataExist = ElevationHelper.getInstance().tilesExist();
        }

        needToLoadCourseData = true;
        needToProccedOnSurfaceCreated = true;
        isPreparingData = false;
    }

    public synchronized void setCourseVectorDataMapInternal(final Map<String, String> courseVectorDataMap) {

        isPreparingData = true;
        if (context != null)
            typeface = Typeface.createFromAsset(context.getAssets(), fontLocation);
        else
            this.typeface = Typeface.DEFAULT;

        try {
            if (courseVectorDataMap.get(COURSE_ID) != null)
                courseId = courseVectorDataMap.get(COURSE_ID);
            if (courseVectorDataMap.get(GPS_DETAILS) != null)
                courseGpsDetailsResponse = new Gson().fromJson(courseVectorDataMap.get(GPS_DETAILS), CourseGpsDetailsResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // courseGpsDetailsResponse.getGPSList().size();

        this.courseVectorDataMap = courseVectorDataMap;

        destroyCourseData();
        elevationDataExist = false;
        tapCalled = false;
        ElevationHelper.getInstance().reset();
        TileStaticData.getInstance().destroy();
        // Layer.resetBasePoints();

        if (!ElevationHelper.getInstance().isElevationDataLoaded()) {

            tapCalled = false;
            destroyCourseData();
            elevationDataExist = false;
            ElevationHelper.getInstance().reset();
            TileStaticData.getInstance().destroy();

            //  Layer.resetBasePoints();
            elevationDataExist = ElevationHelper.getInstance().parseElevationData(courseVectorDataMap.get(ELEVATION), null);
            if (elevationDataExist) {
                //courseVectorDataMap.remove(ELEVATION);
                elevationDataExist = ElevationHelper.getInstance().splitToTiles();
            }
        } else {
            elevationDataExist = ElevationHelper.getInstance().tilesExist();
        }

        needToLoadCourseData = true;
        needToProccedOnSurfaceCreated = true;
        isPreparingData = false;

    }

    public synchronized void setHoleWithinCourse(final HoleWithinCourse holeWithinCourse,
                                                 final boolean resetPosition) {
        setHoleWithinCourse(holeWithinCourse, resetPosition, 0);
    }

    public synchronized void setHoleWithinCourse(final HoleWithinCourse holeWithinCourse,
                                                 final boolean resetPosition,
                                                 final int initialTeeBox) {
        this.initialTeeBox = initialTeeBox;

        if (holeLoadingStateChangedListener != null) {
            holeLoadingStateChangedListener.onStartLoading();
        }

        if (this.holeWithinCourse == null || !this.holeWithinCourse.getCourseId().equals(holeWithinCourse.getCourseId())) {
            needToLoadStaticData = true;
        }

        this.holeWithinCourse = holeWithinCourse;
        this.resetPosition = resetPosition;
        needToChangeHole = true;

        if (currentHoleChangedListener != null) {
            currentHoleChangedListener.onHoleChanged(this.holeWithinCourse);
        }
    }

    public synchronized void setTeeboxAsCurrentLocation(final int teeBoxIndex) {
        initialTeeBox = teeBoxIndex;

        final Location locationGps = getTeeBoxLocationGps();
        setCurrentLocationGPSInternal(locationGps, false, false, false);
    }

    public synchronized void setCameraPos(final Vector v) {
        setCameraPos((float) v.x, (float) v.y);
    }

    public synchronized void setCameraPos(final float x,
                                          final float y) {
        currentXPos = -x;
        currentYPos = -y;
    }

    public synchronized boolean setCurrentLocationGps(final Location locationGps,
                                                      final boolean updateCameraPos) {

        if (currentLocationGPS == null) {
            return false;
        }

        newLocationGps = locationGps;
        this.updateCameraPos = updateCameraPos;
        needToUpdateLocation = true;

        callouts.setCurrentLocation(newLocationGps, this);
        return true;
    }

    /*private synchronized void setCalloutTypeface(final Typeface typeface) {
        if (typeface != null)
            this.typeface = typeface;
        else if (this.typeface == null)
            this.typeface = Typeface.DEFAULT;
    }*/

    public synchronized void setFrontGreeenLocationGPS(final Location locationGps) {
        if (frontGreenMarker == null) {
            frontGreenMarker = new DistanceMarker();
        }

        frontGreenMarker.setLocationGps(locationGps);

        needToUpdateDistances = true;
    }

    public synchronized void setBackGreeenLocationGPS(final Location locationGps) {
        if (backGreenMarker == null) {
            backGreenMarker = new DistanceMarker();
        }

        backGreenMarker.setLocationGps(locationGps);

        needToUpdateDistances = true;
    }

    public String getCourseID() {
        return courseId;
    }

    public synchronized void setHazardList(final List<Location> hazardList) {
        if (hazardMarkerArray != null) {
            if (hazardMarkersToDestroy == null) {
                hazardMarkersToDestroy = new ArrayList<>();
            }
            hazardMarkersToDestroy.addAll(Arrays.asList(hazardMarkerArray));
        }

        final List<DistanceMarker> hazardMarkers = new ArrayList<>();
        for (final Location location : hazardList) {
            DistanceMarker marker = new DistanceMarker();
            marker.setLocationGps(location);
            hazardMarkers.add(marker);
        }
        hazardMarkerArray = hazardMarkers.toArray(new DistanceMarker[hazardMarkers.size()]);

        needToInitializeHazardMarkers = true;
        needToUpdateDistances = true;
    }

    public synchronized void setMetricUnits(final boolean metricUnits) {
        this.metricUnits = metricUnits;
        needToUpdateDistances = true;
    }

    public synchronized boolean isMetricUnits() {
        return metricUnits;
    }

    public synchronized int getNumHoles() {
        return numHoles;
    }

    public synchronized void setParDataMap(final Map<String, Integer[]> parDataMap) {
        this.parDataMap = parDataMap;
    }

    public synchronized boolean isOverallMode(final NavigationMode mode) {
        return mode == NavigationMode.OverallHole3;
    }

    public synchronized boolean isOverallMode() {
        if (needToChangeNavigationMode) {
            return isOverallMode(newNavigationMode);
        }

        return isOverallMode(navigationMode);
    }

    public synchronized void setCalloutsDrawMode(Callouts.CalloutsDrawMode drawMode) {
        if (callouts != null) {
            calloutsDrawMode = drawMode;
            callouts.setCalloutsDrawMode(calloutsDrawMode);
        }
    }

    public void setCalloutsOverlay(Boolean isOverlay) {
        if (callouts != null) {
            this.calloutsShowOverlay = isOverlay;
            callouts.setShowOverlay(calloutsShowOverlay);
        }
    }

    public synchronized void setAheadCartsListGps(final List<Location> aheadCartsListGps) {
        this.aheadCartsListGps = new ArrayList<>(aheadCartsListGps);
    }

    public synchronized boolean isCalloutsVisible() {
        return navigationMode == NavigationMode.GreenView || navigationMode == NavigationMode.Flyover ||
                isOverallMode() || navigationMode == NavigationMode.FreeCam || navigationMode == NavigationMode.FlyoverPause;
    }

    public synchronized void setPinPositionOverrideMap(final Map<String, List<PinPositionOverride>> pinPositionOverrideMap) {
        if (pinPositionOverrideMap != null) {
            this.pinPositionOverrideMap = pinPositionOverrideMap;
            flagLocation = getFlagLocation();
        }
    }

    public void setTeeboxAsCurrentLocation() {
        drawLocationMarker = false;

        final Location locationGps = getTeeBoxLocationGps();
        setCurrentLocationGPSInternal(locationGps, true, true, false);
    }

    public Location getTeeBoxLocation() {
        final Location locationGps = getTeeBoxLocationGps();
        final Location location = new Location("");
        location.setLatitude(Layer.transformLat(locationGps.getLatitude()));
        location.setLongitude(Layer.transformLon(locationGps.getLongitude()));
        return location;
    }

    public Location getTeeBoxLocationGps() {
        final Location locationGps = new Location("");
        locationGps.setLongitude(teeboxcenterGPS.getPointList().get(initialTeeBox).getPointList()[0].x);
        locationGps.setLatitude(teeboxcenterGPS.getPointList().get(initialTeeBox).getPointList()[0].y);
        return locationGps;
    }


    public int getBackgroundTextureId() {
        if (textureSet != null && textureSet.getBackgroundTexture() != null && textureSet.getBackgroundTextureCompressed() != null) {
            return !ETC1Util.isETC1Supported() ? textureSet.getBackgroundTexture() : textureSet.getBackgroundTextureCompressed();
        } else {
            return !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_background : R.raw.v3d_gpsmap_background;
        }
    }

    public int getBackgroundTextureId2D() {
        if (textureSet != null && textureSet.getBackgroundTexture2d() != null && textureSet.getBackgroundTexture2dCompressed() != null) {
            return !ETC1Util.isETC1Supported() ? textureSet.getBackgroundTexture2d() : textureSet.getBackgroundTexture2dCompressed();
        } else {
            return !ETC1Util.isETC1Supported() ? R.drawable.v2d_background : R.raw.v2d_background;
        }
    }

    public int getPerimeterTextureId() {
        if (textureSet != null && textureSet.getPerimeterTexture() != null && textureSet.getPerimeterTextureCompressed() != null)
            return !ETC1Util.isETC1Supported() ? textureSet.getPerimeterTexture() : textureSet.getPerimeterTextureCompressed();
        else
            return !ETC1Util.isETC1Supported() ? R.drawable.v3d_gpsmap_background : R.raw.v3d_gpsmap_background;
    }


    private Float getFreeCamAngle(Location location) {

        Vector startPos = null;
        Vector endPos = centralpath.getFirstPointList().getLastPoint();

        if (location == null) {
            startPos = centralpath.getFirstPointList().getFirstPoint();
            return calculateRotationAngleWithStartPos(startPos, endPos);
        }

        Vector currentLocationVector = new Vector(location);

        if (centralpath != null)
            for (final PointList pointList : centralpath.getPointList()) {
                final Vector[] points = pointList.getPointList();
                for (final Vector point : points) {
                    if (startPos == null) {
                        startPos = point;
                    } else if (currentLocationVector.distance(point) < currentLocationVector.distance(startPos)) {
                        startPos = point;
                    }
                }
            }

        if (startPos != null && startPos.equals(endPos) && centralpath.getFirstPointList().getPointList().length > 1) {
            startPos = new Vector(centralpath.getFirstPointList().getPointList()[centralpath.getFirstPointList().getPointList().length - 2]);
        } else if (startPos != null && startPos.equals(endPos)) {
            startPos = null;
        }

        if (startPos == null) {
            try {
                startPos = new Vector(getTeeBoxLocation());
            } catch (Exception e) {
                startPos = centralpath.getFirstPointList().getFirstPoint();

            }
        }

        return calculateRotationAngleWithStartPos(startPos, endPos);

    }

    public synchronized void setCurrentLocationGPSInternal(final Location locationGPS,
                                                           final boolean recalculateAngle,
                                                           final boolean updateCameraPos, boolean isFromFreeCam) {
        if (distanceLineBuffer == null) {
            ByteBuffer bb = ByteBuffer.allocateDirect(24);
            bb.order(ByteOrder.nativeOrder());
            distanceLineBuffer = bb.asFloatBuffer();
        }

        currentLocationGPS = new Location(locationGPS);
        currentLocation = new Location("");
        currentLocation.setLatitude(Layer.transformLat(currentLocationGPS.getLatitude()));
        currentLocation.setLongitude(Layer.transformLon(currentLocationGPS.getLongitude()));

        distanceLineBuffer.position(0);
        float locationX = (float) currentLocation.getLongitude();
        float locationY = (float) currentLocation.getLatitude();
        distanceLineBuffer.put(locationX);
        distanceLineBuffer.put(locationY);
        distanceLineBuffer.put(0);
        if (flagLocation == null)
            flagLocation = getFlagLocation();
        float flagX = (float) flagLocation.getLongitude();
        float flagY = (float) flagLocation.getLatitude();
        distanceLineBuffer.put(flagX);
        distanceLineBuffer.put(flagY);
        distanceLineBuffer.put(0);
        distanceLineBuffer.position(0);

        if (recalculateAngle) {
            if (navigationMode != NavigationMode.FreeCam)
                currentRotationAngle = calculateRotationAngle(currentLocation);
            else
                currentRotationAngle = getFreeCamAngle(currentLocation);
        }

        if (updateCameraPos) {
            if (navigationMode == NavigationMode.FreeCam || navigationMode == NavigationMode.OverallHole3) {
                final Vector cameraPos = calculateCameraPosition(isFromFreeCam);
                if (cameraPos != null)
                    setCameraPos(cameraPos);
                else
                    setCameraPos(locationX, locationY);
            } else
                setCameraPos(locationX, locationY);
        }

        needToUpdateDistances = true;
        requestRender();
    }

    public synchronized void setFrameRenderListener(final Viewer.FrameRenderListener frameRenderListener) {
        this.frameRenderListener = frameRenderListener;
    }

    public float calculateRotationAngle(final Location location) {
        if (flagLocation == null) {
            getFlagLocation();
        }
        float flagX = (float) flagLocation.getLongitude();
        float flagY = (float) flagLocation.getLatitude();
        float locationX = (float) location.getLongitude();
        float locationY = (float) location.getLatitude();

        float rotationAngle = (float) VectorMath.angle(
                new Vector(locationX, locationY + 1),
                new Vector(locationX, locationY),
                new Vector(flagX, flagY)
        );
        rotationAngle = (float) VectorMath.rad2deg(rotationAngle);
        if (flagX < locationX) {
            rotationAngle *= -1;
        }

        return rotationAngle;
    }

    public float distance(final Location location1,
                          final Location location2) {
        float distance = location1.distanceTo(location2);
        if (!metricUnits) {
            distance *= 1.0936;
        }

        distance = Math.round(distance);
        distance = Math.min(999, distance);

        return distance;
    }

    public Vector calculateCameraPosition(boolean isFromFreeCam) {
        Vector retval = null;
        if (flagLocation == null)
            flagLocation = getFlagLocation();
        for (final PointList pointList : centralpath.getPointList()) {
            final Vector[] points = pointList.getPointList();
            for (int i = 0; i < points.length - 1; i++) {
                retval = VectorMath.calculateProjectionPoint(
                        new Vector(currentLocation), points[i], points[i + 1]);
                if (retval != null) {

                    retval = new Vector(flagLocation)
                            .substracted(retval)
                            .normalized()
                            .multiplied(1.5)
                            .added(retval);

                    break;
                }
            }
        }


        if (navigationMode == NavigationMode.FreeCam && retval == null) {
            final Vector location = new Vector(currentLocation);

            Vector nearestCentralPathPoint = null;

            for (final PointList pointList : centralpath.getPointList()) {
                final Vector[] points = pointList.getPointList();
                for (final Vector point : points) {
                    if (nearestCentralPathPoint == null) {
                        nearestCentralPathPoint = point;
                    } else if (location.distance(point) < location.distance(nearestCentralPathPoint)) {
                        nearestCentralPathPoint = point;
                    }
                }
            }

            if (nearestCentralPathPoint != null) {
                if (needToChangeNavigationMode || isFromFreeCam)
                    return nearestCentralPathPoint;
                else
                    retval = new Vector(nearestCentralPathPoint);
            }
        }

        if (retval == null) {
            final Vector location = new Vector(currentLocation);
            final Vector first = centralpath.getFirstPointList().getFirstPoint();
            final Vector last = centralpath.getFirstPointList().getLastPoint();
            final double dfirst = location.distance(first);
            final double dlast = location.distance(last);
            retval = dfirst < dlast ? first : last;
        }

        if (needToChangeNavigationMode || isFromFreeCam)
            return retval;

        try {


            if (elevationDataExist && ElevationHelper.getInstance().tilesExist()) {
                double alt = ElevationHelper.getInstance().getHeightInPoint(retval, (Course3DRenderer) this);
                double shift = Math.abs(alt * Math.tan(VectorMath.deg2rad(currentViewAngle)));
                retval.x = (float) (-retval.x + Math.cos(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
                retval.y = (float) (-retval.y - Math.sin(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);


                retval.x = -retval.x;
                retval.y = -retval.y;

            }
        } catch (Exception e) {

        }

        return retval;
    }

    public Location getOverridedPinPosition() {
        Location retval = null;

        final List<PinPositionOverride> pinPositionOverrides = pinPositionOverrideMap.get(holeWithinCourse.getCourseId());
        if (pinPositionOverrides == null) {
            return retval;
        }

        for (final PinPositionOverride override : pinPositionOverrides) {
            final DateTime todayStartOfDay = new DateTime(DateTimeZone.getDefault()).withMillisOfDay(0);
            if (override.getHoleNumber() == holeWithinCourse.getHoleNumber() && todayStartOfDay.equals(override.getDateTime())) {
                retval = override.getPinPosition();
            }
        }

        return retval;
    }

    public synchronized boolean isCurrentLocationVisible() {
        return currentLocationVisible;
    }

    public synchronized void setTextureSet(final TextureSet textureSet) {
        if (textureSet != null) {
            if (textureSet instanceof DesertTextureSet)
                textureSetType = TextureSetType.TROPICAL;
            else if (textureSet.isCustom())
                textureSetType = TextureSetType.CUSTOM;
            else
                textureSetType = TextureSetType.DEFAULT;
        }
        this.textureSet = textureSet;
    }

    public void setCartLocationVisible(final boolean visible) {
        cartLocationVisible = visible;
    }

    protected Vector unprojectLocation(final float x,
                                       final float y,
                                       final float z,
                                       final float rotationAngle,
                                       final float viewAngle,
                                       final float locationX,
                                       final float locationY) {
        return projectionCalculator.unprojectLocation(x, y, z, rotationAngle, viewAngle, locationX, locationY);
    }

    protected Vector unprojectVectorAbs(final float x,
                                        final float y,
                                        final float z,
                                        final float rotationAngle,
                                        final float viewAngle,
                                        final Vector vector) {
        final Vector retval = unprojectLocation(x, y, z, rotationAngle, viewAngle,
                (float) vector.x, (float) vector.y);
        retval.x = Math.abs(retval.x);
        retval.y = Math.abs(retval.y);
        return retval;
    }

    protected void applyGreenView(final float rotationAngle,
                                  final float viewAngle) {
        Integer parValue = -1;
        try {
            parValue = parDataMap.get(holeWithinCourse.getCourseId())[holeWithinCourse.getHoleNumber() - 1];
        } catch (Exception e) {

        }

        if (parValue != -1) {
            applyGreenViewForPar3();
        } else
            applyGreenViewGeneral(rotationAngle, viewAngle);
    }

    protected void applyGreenViewGeneral(final float rotationAngle,
                                         final float viewAngle) {

        Vector cameraPoint;

        if (frontGreenMarker != null && frontGreenMarker.getLocationGps() != null &&
                backGreenMarker != null && backGreenMarker.getLocationGps() != null) {

            Vector backGreenPos = new Vector();
            backGreenPos.y = Layer.transformLat(backGreenMarker.getLocationGps().getLatitude());
            backGreenPos.x = Layer.transformLon(backGreenMarker.getLocationGps().getLongitude());

            Vector frontGreenPos = new Vector();
            frontGreenPos.y = Layer.transformLat(frontGreenMarker.getLocationGps().getLatitude());
            frontGreenPos.x = Layer.transformLon(frontGreenMarker.getLocationGps().getLongitude());

            Vector cameraPos = VectorMath.centeroid(green.getExtremeLeft(), green.getExtremeTop(),
                    green.getExtremeRight(), green.getExtremeBottom());
            cameraPos = cameraPos
                    .substracted(frontGreenPos)
                    .multiplied(0.7)
                    .added(frontGreenPos);

            final float extremeThreshold = 0.7f;
            final float markerThreshold = 0.5f;
            final float camX = -(float) cameraPos.x;
            final float camY = -(float) cameraPos.y;
            float camZ;
            final RectF extremeCheckRect = new RectF(0, 0, extremeThreshold, extremeThreshold);
            final RectF markerCheckRect = new RectF(0, 0, markerThreshold, markerThreshold);
            Integer extremeLeft = null;
            Integer extremeTop = null;
            Integer extremeRight = null;
            Integer extremeBottom = null;
            List<Vector> projectedPointList = new ArrayList<>();
            List<Vector> pointList = new ArrayList<>();

            try {
               /* final JSONObject holesObject = ((JSONObject)courseVectorDataMap.get(holeWithinCourse.getCourseId())).getJSONObject("Holes");
                final JSONArray holeArray = holesObject.getJSONArray("Hole");
                final JSONObject holeObject = holeArray.getJSONObject(holeWithinCourse.getHoleNumber() - 1);*/

                BaseShapeObject green = vectorGPSObject.getVectorGPSObject().getHoles().getHoles().get(holeWithinCourse.getHoleNumber() - 1).getGreen();

                final int shapeCount = green.getShapeCount();

                for (int i = 0; i < shapeCount; i++) {
                    final String points = green.getShapes().getShape().get(i).getPoints();
                    final String[] lonLatPointsArray = points.split(",");
                    for (int j = 0; j < lonLatPointsArray.length; j++) {
                        final String lonLatPoints = lonLatPointsArray[j];
                        final String[] lonLatPair = lonLatPoints.split(" ");

                        final float lat = (float) Layer.transformLat(lonLatPair[1]);
                        final float lon = (float) Layer.transformLon(lonLatPair[0]);

                        final Vector projectedVector = unprojectLocation(camX, camY, -20.0f, rotationAngle, viewAngle, lon, lat);
                        if (extremeLeft == null) {
                            extremeLeft = j;
                            extremeTop = j;
                            extremeRight = j;
                            extremeBottom = j;
                        } else {
                            if (projectedVector.x < projectedPointList.get(extremeLeft).x) {
                                extremeLeft = j;
                            }
                            if (projectedVector.x > projectedPointList.get(extremeRight).x) {
                                extremeRight = j;
                            }
                            if (projectedVector.y < projectedPointList.get(extremeBottom).y) {
                                extremeBottom = j;
                            }
                            if (projectedVector.y > projectedPointList.get(extremeTop).y) {
                                extremeTop = j;
                            }
                        }
                        projectedPointList.add(projectedVector);
                        pointList.add(new Vector(lon, lat));
                    }
                }
            } catch (Exception e) {
                getViewerLogger().error(e);
            }

            // debug
//            frontGreenMarker.setPosition(pointList.get(extremeTop));
//            backGreenMarker.setPosition(pointList.get(extremeBottom));

            for (camZ = 0; camZ > -20; camZ -= 0.1f) {
                final Vector pt1 = unprojectVectorAbs(camX, camY, camZ,
                        rotationAngle, viewAngle,
                        pointList.get(extremeLeft));

                final Vector pt2 = unprojectVectorAbs(camX, camY, camZ,
                        rotationAngle, viewAngle,
                        pointList.get(extremeTop));

                final Vector pt3 = unprojectVectorAbs(camX, camY, camZ,
                        rotationAngle, viewAngle,
                        pointList.get(extremeRight));

                final Vector pt4 = unprojectVectorAbs(camX, camY, camZ,
                        rotationAngle, viewAngle,
                        pointList.get(extremeBottom));

                final Vector pt5 = unprojectVectorAbs(camX, camY, camZ,
                        rotationAngle, viewAngle,
                        backGreenPos);

                final Vector pt6 = unprojectVectorAbs(camX, camY, camZ,
                        rotationAngle, viewAngle,
                        frontGreenPos);

                if (pt1.isInsideRectF(extremeCheckRect) && pt2.isInsideRectF(extremeCheckRect) &&
                        pt3.isInsideRectF(extremeCheckRect) && pt4.isInsideRectF(extremeCheckRect) &&
                        pt5.isInsideRectF(markerCheckRect) && pt6.isInsideRectF(markerCheckRect)) {
                    break;
                }
            }

            currentZ = camZ;
            cameraPoint = cameraPos;

        } else {
            currentZ = -2.5f;
            cameraPoint = greencenter.getFirstPointList().getFirstPoint();
        }

        greenViewZPos = currentZ;
        currentViewAngle = GREEN_VIEW_VIEW_ANGLE;

        if (elevationDataExist && ElevationHelper.getInstance().tilesExist()) {
            double alt = ElevationHelper.getInstance().getHeightInPoint(cameraPoint.x, cameraPoint.y, (Course3DRenderer) this);
            currentViewAngle = OVERALL_HOLE_3_VIEW_ANGLE;
            double shift = Math.abs(alt * Math.tan(VectorMath.deg2rad(currentViewAngle)));
            offsetX = 0;
            offsetY = 0;
            currentXPos = (float) (-cameraPoint.x + Math.cos(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
            currentYPos = (float) (-cameraPoint.y - Math.sin(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
            currentZ = (float) (currentZ - flyoverParameters.getEndViewShift());
            greenViewZPos = currentZ;
            viewShift = calculateViewOffset();
        } else {
            setCameraPos(cameraPoint);
        }

        if (navigationModeChangedListener != null) {
            navigationModeChangedListener.onNavigationModeChanged(this.navigationMode);
        }
    }


    protected void applyGreenViewForPar3() {

        Vector cameraPoint;

        if (frontGreenMarker != null && frontGreenMarker.getLocationGps() != null &&
                backGreenMarker != null && backGreenMarker.getLocationGps() != null) {

            Vector backGreenPos = new Vector();
            backGreenPos.y = Layer.transformLat(backGreenMarker.getLocationGps().getLatitude());
            backGreenPos.x = Layer.transformLon(backGreenMarker.getLocationGps().getLongitude());

            Vector frontGreenPos = new Vector();
            frontGreenPos.y = Layer.transformLat(frontGreenMarker.getLocationGps().getLatitude());
            frontGreenPos.x = Layer.transformLon(frontGreenMarker.getLocationGps().getLongitude());

            Vector cameraPos = VectorMath.centeroid(green.getExtremeLeft(), green.getExtremeTop(),
                    green.getExtremeRight(), green.getExtremeBottom());

            cameraPos = cameraPos
                    .substracted(frontGreenPos)
                    .multiplied(0.7)
                    .added(frontGreenPos);
            cameraPoint = cameraPos;

        } else {
            currentZ = -2.5f;
            cameraPoint = greencenter.getFirstPointList().getFirstPoint();
        }


        greenViewZPos = currentZ;
        currentViewAngle = GREEN_VIEW_VIEW_ANGLE;
        if (elevationDataExist && ElevationHelper.getInstance().tilesExist()) {
            double alt = ElevationHelper.getInstance().getHeightInPoint(cameraPoint.x, cameraPoint.y, (Course3DRenderer) this);
            currentViewAngle = OVERALL_HOLE_3_VIEW_ANGLE;
            double shift = Math.abs(alt * Math.tan(VectorMath.deg2rad(currentViewAngle)));
            offsetX = 0;
            offsetY = 0;
            currentXPos = (float) (-cameraPoint.x + Math.cos(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
            currentYPos = (float) (-cameraPoint.y - Math.sin(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
            currentZ = (float) calculateGreenViewCameraZ();
            greenViewZPos = currentZ;
            viewShift = calculateViewOffset();
        } else {
            setCameraPos(cameraPoint);
            currentZ = (float) calculateGreenViewCameraZ();
        }
        greenViewZPos = currentZ;

        if (navigationModeChangedListener != null) {
            navigationModeChangedListener.onNavigationModeChanged(this.navigationMode);
        }
    }


    private double calculateGreenViewCameraZ() {
        double retVal = 0;
        float greenZ;

        for (greenZ = 0; greenZ > -200; greenZ = greenZ - 0.1f) {
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, greenZ);
            Matrix.rotateM(modelViewMatrix, 0, -currentViewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, currentRotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, currentXPos, currentYPos, 0);
            Frustum.updateFrustum((Course3DRenderer) this);
            if (Frustum.isLayerInFrustum(green, (Course3DRenderer) this)) {
                retVal = greenZ;
                break;
            }
        }
        return retVal - 1;
    }

    private boolean isGreenViewFullyDisplayed() {
        Matrix.setIdentityM(modelViewMatrix, 0);
        Matrix.translateM(modelViewMatrix, 0, 0, 0, currentZ);
        Matrix.rotateM(modelViewMatrix, 0, -currentViewAngle, 1, 0, 0);
        Matrix.rotateM(modelViewMatrix, 0, currentRotationAngle, 0, 0, 1);
        Matrix.translateM(modelViewMatrix, 0, currentXPos, currentYPos, 0);
        Frustum.updateFrustum((Course3DRenderer) this);
        return Frustum.isLayerInFrustum(green, (Course3DRenderer) this);
    }

    protected float calculateDistanceMarkersScale() {
        float fontScale = DistanceMarker.DEFAULT_SCALE;
        if (isOverallMode()) {
            fontScale = DistanceMarker.OVERALL_SCALE;
        } else if (navigationMode == NavigationMode.FreeCam) {
            fontScale = DistanceMarker.FREECAM_SCALE;
        } else if (navigationMode == NavigationMode.GreenView) {
            fontScale = DistanceMarker.GREEN_VIEW_SCALE;
        }
        return fontScale;
    }

    public synchronized void startZoom() {

    }

    public synchronized void zoom(final float factor) {
        if (currentZ * factor < MAX_Z_2D_VIEW) {
            return;
        }
        if (currentZ * factor > MIN_Z_2D_VIEW) {
            return;
        }
        currentZFactor = factor;

    }

    public synchronized void onDoubleTap(float factor, float x, float y) {
        boolean isCurrentZMinimum = currentZ == MIN_Z_2D_VIEW;
        if (currentZ * factor < MAX_Z_2D_VIEW) {
            factor = MAX_Z_2D_VIEW / currentZ;
        }
        if (currentZ * factor > MIN_Z_2D_VIEW || (MIN_Z_2D_VIEW - ((currentZ * factor)) < 2)) {
            factor = MIN_Z_2D_VIEW / currentZ;
        }
        Vector vector = unprojectWithTouchPoint(x, y);

        if (!isCurrentZMinimum) {
            this.currentXPos = (float) -vector.x;
            this.currentYPos = (float) -vector.y;
        }

        currentZFactor = factor;
        currentZ = currentZ * currentZFactor;
        currentZFactor = 1;

        stopZoomTimeHandler();
        startZoomTimeHandler();
    }

    protected void addLog(String mes) {
        Log.e(getClass().getSimpleName(), mes);
    }

    public synchronized void endZoom() {

        currentZ = currentZ * currentZFactor;
        currentZFactor = 1;
        if (navigationMode == NavigationMode.NavigationMode2D) {
            currentZ = Math.min(MIN_Z_2D_VIEW, z);
            currentZ = Math.max(MAX_Z_2D_VIEW, z);
        }
    }

    public synchronized void startRotation() {
    }

    public synchronized void rotate(final float angle) {
        currentRotation = angle * (float) Math.PI;
    }

    public synchronized void endRotation() {
        currentRotationAngle = currentRotationAngle + currentRotation;
        currentRotation = 0;
    }

    public void startMove(final float moveX, final float moveY) {
        if (callouts == null)
            return;
        callouts.setFocus(false);
        if (navigationMode == NavigationMode.NavigationMode2D) {
            callouts.startMove(this, unprojectWithTouchPoint(moveX, moveY));
        }
    }

    public synchronized void move(final float x, final float y, final float moveX, final float moveY) {

        if (navigationMode == null)
            return;

        boolean handled = false;
        gesturePan.x = (int) x;
        gesturePan.y = (int) y;
        gestureMovePan.x = (int) moveX;
        gestureMovePan.y = (int) moveY;
        if (navigationMode == NavigationMode.NavigationMode2D)
            handled = callouts.onMove(unprojectWithTouchPoint(moveX, moveY, true));
        if (!handled) {
            if (moveDirection == null)
                moveDirection = new Vector();
            moveDirection.x = VectorMath.rotated(new Vector(x, y), VectorMath.deg2rad(rotationAngle)).x;
            moveDirection.y = VectorMath.rotated(new Vector(x, y), VectorMath.deg2rad(rotationAngle)).y;
            float zfactor = currentZ / FREE_CAM_ZPOS;
            offsetX = (float) (-moveDirection.x / (navigationMode == NavigationMode.NavigationMode2D ? 400 : 300) * zfactor);
            offsetY = (float) (moveDirection.y / (navigationMode == NavigationMode.NavigationMode2D ? 400 : 300) * zfactor);
        }

        if (navigationMode != NavigationMode.NavigationMode2D && ElevationHelper.getInstance().tilesExist()) {

            cameraPoint = getCameraProjectionPoint();
            double cameraProjectionAlt = ElevationHelper.getInstance().getHeightInPoint(cameraPoint, (Course3DRenderer) this);

            switch (navigationMode) {
                case Flyover:
                    currentZ = (float) Math.min((FLYOVER_ZPOS - (cameraProjectionAlt / Math.cos(VectorMath.deg2rad(currentViewAngle)))), flyoverParameters.getDefaultZoom());
                    break;
                case OverallHole3:
                    currentZ = (float) normalizeZ(overallHoleStartZ - Math.max(cameraProjectionAlt / Math.cos(VectorMath.deg2rad(currentViewAngle)), flyoverParameters.getDefaultZoom()));
                    break;
                case FlyoverPause:
                    currentZ = (float) normalizeZ(Math.min((FLYOVER_ZPOS - (cameraProjectionAlt / Math.cos(VectorMath.deg2rad(currentViewAngle)))), flyoverParameters.getDefaultZoom()));
                    break;
                case GreenView:
                    double retVal = -(cameraProjectionAlt / Math.cos(VectorMath.deg2rad(currentViewAngle))) - 2.5;
                    currentZ = (float) normalizeZ(Math.min(greenViewZPos, retVal));
                    break;
                case FreeCam:
                    currentZ = (float) normalizeZ(Math.min((FREE_CAM_ZPOS - (cameraProjectionAlt / Math.cos(VectorMath.deg2rad(currentViewAngle)))), flyoverParameters.getDefaultZoom()));
                    break;
            }
        }
    }

    public void endMove(final float x, final float y, final float moveX, final float moveY) {
        boolean handled = false;
        if (navigationMode == NavigationMode.NavigationMode2D)
            handled = callouts.endMove(unprojectWithTouchPoint(moveX, moveY));

        if (!handled) {
            currentXPos = currentXPos + offsetX;
            offsetX = 0;
            currentYPos = currentYPos + offsetY;
            offsetY = 0;
        }

        gesturePan = new Point(0, 0);
        gestureMovePan = new Point(0, 0);
    }

    public synchronized Vector unprojectWithTouchPoint(float x,
                                                       float y) {

        final Ray ray = new Ray(viewport.x, viewport.y, x, y, modelViewMatrix, projectionMatrix);

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
                final float[] unprojected = new float[4];
                Matrix.multiplyMV(unprojected, 0, intersectionUnprojectionMatrix, 0, intersectionPoint, 0);
                return new Vector(unprojected[0], unprojected[1]);
            }
        }
        return null;

    }

    public synchronized Vector unprojectWithTouchPoint(float x,
                                                       float y, boolean a) {

        final Ray ray = new Ray(viewport.x, viewport.y, x, y, modelViewMatrix, projectionMatrix);

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
                final float[] unprojected = new float[4];
                Matrix.multiplyMV(unprojected, 0, intersectionUnprojectionMatrix, 0, intersectionPoint, 0);
                return new Vector(unprojected[0], unprojected[1]);
            }
        }
        return null;

    }

    public int[] loadIntArray(final int resourceId,
                              final Context context) {
        final TypedArray typedArray = context.getResources().obtainTypedArray(resourceId);
        final int[] retval = new int[typedArray.length()];
        for (int i = 0; i < typedArray.length(); i++) {
            retval[i] = typedArray.getResourceId(i, 0);
        }

        typedArray.recycle();
        return retval;
    }

    public synchronized void updateDistanceMarkers() {
        if (currentLocationGPS == null) {
            return;
        }

        if (frontGreenMarker != null && frontGreenMarker.getLocationGps() != null) {
            frontGreenMarker.setDistance(distance(currentLocationGPS, frontGreenMarker.getLocationGps()));
            frontGreenMarker.recalculatePosition();
        }

        if (backGreenMarker != null && backGreenMarker.getLocationGps() != null) {
            backGreenMarker.setDistance(distance(currentLocationGPS, backGreenMarker.getLocationGps()));
            backGreenMarker.recalculatePosition();
        }

        if (hazardMarkerArray != null)
            for (final DistanceMarker marker : hazardMarkerArray) {
                if (marker.getLocationGps() != null) {
                    marker.setDistance(distance(currentLocationGPS, marker.getLocationGps()));
                    marker.recalculatePosition();
                }
            }

        if (tapDistanceMarker != null && tapDistanceMarker.getLocationGps() != null) {
            tapDistanceMarker.setDistance(distance(currentLocationGPS, tapDistanceMarker.getLocationGps()));
            tapDistanceMarker.recalculatePosition();
        }
    }

    private float overallHoleStartZ = -5;

    public void applyOverallHole3() {
        Vector farPoint = null;
        Vector nearPoint = null;
        double maxDistance = 0;
        double minDistance = 0;
        if (flagLocation == null)
            flagLocation = getFlagLocation();
        final Vector flagLocationVector = new Vector(flagLocation);
        if (fairway != null) {
            for (final PointList pointList : fairway.getPointList()) {
                for (final Vector point : pointList.getPointList()) {
                    double distance = point.distance(flagLocationVector);

                    if (farPoint == null) {
                        farPoint = point;
                        nearPoint = point;
                        maxDistance = distance;
                        minDistance = distance;
                        continue;
                    }

                    if (distance > maxDistance) {
                        farPoint = point;
                        maxDistance = distance;
                    }

                    if (distance < minDistance) {
                        nearPoint = point;
                        minDistance = distance;
                    }
                }
            }
        }

        final Integer parValue = parDataMap.get(holeWithinCourse.getCourseId())[holeWithinCourse.getHoleNumber() - 1];
        ParViewConfig config = sParZoomLevels3.get(0);
        if (parDataMap != null) {
            if (sParZoomLevels3.containsKey(parValue)) {
                config = sParZoomLevels3.get(parValue);
            }
        }

        Vector overallCameraPoint = null;
        if (parValue == 3) {
            final Vector[] path = new Vector[]{
                    teeboxcenter.getFirstPointList().getFirstPoint(),
                    new Vector(flagLocation)
            };
            overallCameraPoint = new Path(path).getPosition(config.advanceTeebox);
            currentZ = config.zoomTeebox;
        } else if (farPoint == null && nearPoint == null) {
            final Vector[] path = new Vector[]{
                    teeboxcenter.getFirstPointList().getFirstPoint(),
                    new Vector(flagLocation)
            };
            overallCameraPoint = new Path(path).getPosition(config.advanceTeebox);
            currentZ = config.zoomTeebox;
        } else {
            overallCameraPoint = nearPoint
                    .substracted(farPoint)
                    .normalized()
                    .multiplied(config.advanceFairway)
                    .added(farPoint);
            currentZ = config.zoomFairway;
        }


        overallHoleStartZ = currentZ;

        if (elevationDataExist && ElevationHelper.getInstance().tilesExist()) {

            Vector startPos = centralpath.getFirstPointList().getFirstPoint();
            Vector endPos = centralpath.getFirstPointList().getLastPoint();
            currentRotationAngle = calculateRotationAngleWithStartPos(startPos, endPos);
            currentViewAngle = OVERALL_HOLE_3_VIEW_ANGLE;

            double alt = ElevationHelper.getInstance().getHeightInPoint(overallCameraPoint.x, overallCameraPoint.y, (Course3DRenderer) this);

            double shift = Math.abs(alt * Math.tan(VectorMath.deg2rad(currentViewAngle)));
            shift += 1;
            offsetX = 0;
            offsetY = 0;
            currentXPos = (float) (-overallCameraPoint.x + Math.cos(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);
            currentYPos = (float) (-overallCameraPoint.y - Math.sin(VectorMath.deg2rad(90f + currentRotationAngle)) * shift);

            currentZ = (float) (currentZ - flyoverParameters.getHoleAltitude());
            viewShift = calculateViewOffset();
            currentViewAngle = OVERALL_HOLE_3_VIEW_ANGLE;

            double overallExtraZ = calculateOverallExtraZ(farPoint, nearPoint, currentXPos, currentYPos, currentRotationAngle, currentViewAngle, currentZ, parValue);

            if (parValue == 3) {

                Vector cameraPoint = getCameraProjectionPoint();
                double cameraProjectionAlt = ElevationHelper.getInstance().getHeightInPoint(cameraPoint, (Course3DRenderer) this);
                double retVal = (float) normalizeZ(overallHoleStartZ - Math.max(cameraProjectionAlt / Math.cos(VectorMath.deg2rad(currentViewAngle)), flyoverParameters.getDefaultZoom()));

                if (retVal < overallExtraZ)
                    overallExtraZ = retVal;
                overallExtraZ -= 0.8;
            } else {
                overallExtraZ -= 0.5;
            }

            /*TODO CHECK THIS
            if (overallExtraZ < currentZ) {
                addLog(" nonono");
                //return;
            }*/

            currentZ = (float) overallExtraZ;
        } else {
            setCameraPos(overallCameraPoint);
            currentViewAngle = OVERALL_HOLE_3_VIEW_ANGLE;
            currentRotationAngle = calculateRotationAngle(getTeeBoxLocation());
        }

    }

//    private double calculateOverallExtraZ(Vector farPoint, Vector nearPoint, float posX, float posY, float rotationAngle, float viewAngle, double retVal, int parValue) {
//        float overallZ;
//
//        boolean isTeeBoxExist = false;
//        Vector teeBoxCenter = null;
//        if (parValue == 3 && teeboxcenter != null && teeboxcenter.getFirstPointList() != null && teeboxcenter.getFirstPointList().getPointList().length > 0 && teeboxcenter.getFirstPointList().getFirstPoint() != null) {
//            isTeeBoxExist = true;
//            teeBoxCenter = teeboxcenter.getFirstPointList().getFirstPoint();
//            teeBoxCenter.z = ElevationHelper.getInstance().getHeightInPoint(teeBoxCenter, (Course3DRenderer) this);
//        }
//        if (farPoint == null || nearPoint == null) {
//            addLog(" ----------------   calculateOverallExtraZ  ------------------");
//            addLog(farPoint == null ? "farPoint " : " nearPoint " + " is null");
//            return retVal;
//        }
//        farPoint.z = ElevationHelper.getInstance().getHeightInPoint(farPoint, (Course3DRenderer) this);
//        nearPoint.z = ElevationHelper.getInstance().getHeightInPoint(nearPoint, (Course3DRenderer) this);
//
//        float xTest = currentXPos + offsetX;
//        float yTest = currentYPos + offsetY;
//        float zTest;
//        for (overallZ = 0; overallZ > -200; overallZ = overallZ - 0.1f) {
//            zTest = overallZ * currentZFactor;
//            Matrix.setIdentityM(modelViewMatrix, 0);
//            Matrix.translateM(modelViewMatrix, 0, 0, 0, zTest);
//            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
//            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
//            Matrix.translateM(modelViewMatrix, 0, xTest, yTest, 0);
//
//            Frustum.updateFrustum((Course3DRenderer) this);
//            if (parValue != 3 || (parValue == 3 && !isTeeBoxExist)) {
//                if (Frustum.isPointInFrustum(farPoint) && Frustum.isPointInFrustum(nearPoint)) {
//                    retVal = overallZ;
//                    break;
//                }
//            } else {
//                if (Frustum.isPointInFrustum(farPoint) && Frustum.isPointInFrustum(nearPoint) && Frustum.isPointInFrustum(teeBoxCenter)) {
//                    retVal = overallZ;
//                    break;
//                }
//            }
//        }
//        return retVal;
//    }

    private double calculateOverallExtraZ(Vector farPoint, Vector nearPoint, float posX, float posY, float rotationAngle, float viewAngle, double retVal, int parValue) {
        float overallZ;
        boolean isTeeBoxExist = false;
        Vector teeBoxCenter = null;
        if (parValue == 3 && teeboxcenter != null && teeboxcenter.getFirstPointList() != null && teeboxcenter.getFirstPointList().getPointList().length > 0 && teeboxcenter.getFirstPointList().getFirstPoint() != null) {
            isTeeBoxExist = true;
            teeBoxCenter = teeboxcenter.getFirstPointList().getFirstPoint();
            teeBoxCenter.z = ElevationHelper.getInstance().getHeightInPoint(teeBoxCenter, (Course3DRenderer) this);
        }
        if (farPoint != null) {
            farPoint.z = ElevationHelper.getInstance().getHeightInPoint(farPoint, (Course3DRenderer) this);
        }
        if (nearPoint != null) {
            nearPoint.z = ElevationHelper.getInstance().getHeightInPoint(nearPoint, (Course3DRenderer) this);
        }


        float xTest = currentXPos + offsetX;
        float yTest = currentYPos + offsetY;
        float zTest;
        for (overallZ = 0; overallZ > -200; overallZ = overallZ - 0.1f) {
            zTest = overallZ * currentZFactor;
            Matrix.setIdentityM(modelViewMatrix, 0);
            Matrix.translateM(modelViewMatrix, 0, 0, 0, zTest);
            Matrix.rotateM(modelViewMatrix, 0, -viewAngle, 1, 0, 0);
            Matrix.rotateM(modelViewMatrix, 0, rotationAngle, 0, 0, 1);
            Matrix.translateM(modelViewMatrix, 0, xTest, yTest, 0);

            Frustum.updateFrustum((Course3DRenderer) this);
            if (parValue != 3 || (parValue == 3 && !isTeeBoxExist)) {
                if (farPoint != null && nearPoint != null) {
                    if (Frustum.isPointInFrustum(farPoint) && Frustum.isPointInFrustum(nearPoint)) {
                        retVal = overallZ;
                        break;
                    }
                } else if (farPoint == null && nearPoint == null) {
                    break;
                } else if (farPoint == null && nearPoint != null) {
                    if (Frustum.isPointInFrustum(nearPoint)) {
                        retVal = overallZ;
                        break;
                    }
                } else if (nearPoint == null && farPoint != null) {
                    if (Frustum.isPointInFrustum(farPoint)) {
                        retVal = overallZ;
                        break;
                    }
                }

            } else {
                if (farPoint != null && nearPoint != null) {
                    if (Frustum.isPointInFrustum(farPoint) && Frustum.isPointInFrustum(nearPoint) && Frustum.isPointInFrustum(teeBoxCenter)) {
                        retVal = overallZ;
                        break;
                    }
                } else if (farPoint == null && nearPoint == null) {
                    if (Frustum.isPointInFrustum(teeBoxCenter)) {
                        retVal = overallZ;
                        break;
                    }
                } else if (farPoint == null && nearPoint != null) {
                    if (Frustum.isPointInFrustum(nearPoint) && Frustum.isPointInFrustum(teeBoxCenter)) {
                        retVal = overallZ;
                        break;
                    }
                } else if (nearPoint == null && farPoint != null) {
                    if (Frustum.isPointInFrustum(farPoint) && Frustum.isPointInFrustum(teeBoxCenter)) {
                        retVal = overallZ;
                        break;
                    }
                }
            }
        }
        return retVal;
    }


    public float calculateRotationAngleWithStartPos(Vector startPos, Vector endPos) {
        Vector middlePoint = new Vector(startPos);
        Vector startPoint = new Vector(middlePoint.x, middlePoint.y + 1);
        Vector endPoint = new Vector(endPos);
        double rotationAngle = 0;
        rotationAngle = VectorMath.angle(startPoint, middlePoint, endPoint);
        rotationAngle = VectorMath.rad2deg(rotationAngle);
        if (endPoint.x < middlePoint.x)
            rotationAngle *= -1;
        return (float) rotationAngle;
    }

    protected void invalidateHazardMarkers() {
        if (hazardMarkerArray != null)
            for (DistanceMarker marker : hazardMarkerArray) {
                marker.invalidate();
            }
        needToInitializeHazardMarkers = true;
    }


    public void applyHole2D(boolean initial) {
        Vector startPos = initial ? centralpath.getFirstPointList().getFirstPoint() : callouts.getStartLocation();
        Vector endPos = initial ? centralpath.getFirstPointList().getLastPoint() : callouts.getEndLocation();
        rotationAngle = calculateRotationAngleWithStartPos(startPos, endPos);
        Vector position;
        if (initial) {
            Vector centeroid = VectorMath.centeroid(perimeterPointListLayer.getFirstPointList().getPointList());
            Vector middle = VectorMath.multiplied(VectorMath.added(startPos, endPos), 0.5);
            position = VectorMath.multiplied(VectorMath.added(middle, centeroid), 0.5);
        } else {
            position = VectorMath.multiplied(VectorMath.added(startPos, endPos), 0.5);
        }

        this.currentXPos = -(float) position.x;
        this.currentYPos = -(float) position.y;
        int zoom = (int) MIN_Z_2D_VIEW;
        float treshold = 0.9f;
        while (zoom > MAX_Z_2D_VIEW) {
            this.currentZ = zoom;
            Vector projectedEnd = unprojectVectorAbs(currentXPos, currentYPos, currentZ, rotationAngle, viewAngle, endPos);
            Vector projectedStart = unprojectVectorAbs(currentXPos, currentYPos, currentZ, rotationAngle, viewAngle, startPos);
            if (Math.abs(projectedStart.y) < treshold && Math.abs(projectedEnd.y) < treshold)
                break;
            zoom -= 1;
        }

        if (zoom <= MAX_Z_2D_VIEW) {
            int i;
            final int maxOffest = 100;
            for (i = 0; i < maxOffest; i++) {
                Vector adjustmentVector = VectorMath.added(VectorMath.multiplied(VectorMath.normalized(VectorMath.subtracted(startPos, position)), i), position);
                this.currentXPos = -(float) adjustmentVector.x;
                this.currentYPos = -(float) adjustmentVector.y;
                Vector projectedStart = unprojectVectorAbs(currentXPos, currentYPos, currentZ, rotationAngle, viewAngle, startPos);
                if (Math.abs(projectedStart.y) < treshold) {
                    break;
                }
            }

            if (i == maxOffest) {
                this.currentXPos = -(float) startPos.x;
                this.currentYPos = -(float) startPos.y;
            }
        }
        lastZoomTime = System.currentTimeMillis();
        stopZoomTimeHandler();
        callouts.resetPosition();
    }


    public void applyVBOZoom(Tile tile) {
        this.currentXPosVBO = tile.getCenterX();
        this.currentYPosVBO = tile.getCenterY();
        this.currentZPosVBO = tile.getCenterZ();
    }

    //-------------------- LAYERS -------------------//

    protected Layer addLayer(BaseShapeObject baseShapeObject,
                             String layerName,
                             int resId,
                             Layer.PointInterpolation interpolation,
                             boolean isTextureCompressed) {
        if (baseShapeObject == null)
            return null;
        return addLayer(baseShapeObject, layerName, resId, 0, interpolation, isTextureCompressed);
    }

    protected Layer addLayer(final BaseShapeObject baseShapeObject,
                             final String layerName,
                             final int resId,
                             boolean isTextureCompressed) {
        if (baseShapeObject == null)
            return null;
        return addLayer(baseShapeObject, layerName, resId, 0, Layer.PointInterpolation.Interpolate, isTextureCompressed);
    }

    protected Layer addLayer(final BaseShapeObject baseShapeObject,
                             final String layerName,
                             final int resId,
                             final float extension,
                             final Layer.PointInterpolation interpolation,
                             boolean isTextureCompressed) {
        if (baseShapeObject == null)
            return null;
        Layer layer = loadLayer(baseShapeObject, layerName, resId, extension, interpolation, isTextureCompressed);
        if (layerName.toLowerCase().trim().equals("perimeter") || layerName.toLowerCase().trim().equals("bunker"))
            layer.setName(layerName.toLowerCase());
        layerList.add(layer);

        return layer;
    }

//    protected Bunker3DLayer bunker3DLayer;
//
//    protected Bunker3DLayer addBunkerLayer(final BaseShapeObject baseShapeObject,
//                                           final String layerName,
//                                           final int resId,
//                                           boolean isTextureCompressed) {
//        if (baseShapeObject == null)
//            return null;
//        return addBunkerLayer(baseShapeObject, layerName, resId, 0, Layer.PointInterpolation.Interpolate, isTextureCompressed);
//    }
//
//    protected Bunker3DLayer addBunkerLayer(final BaseShapeObject baseShapeObject,
//                                           final String layerName,
//                                           final int resId,
//                                           final float extension,
//                                           final Layer.PointInterpolation interpolation,
//                                           boolean isTextureCompressed) {
//        if (baseShapeObject == null)
//            return null;
//        bunker3DLayer = loadBunkerLayer(baseShapeObject, layerName, resId, extension, interpolation, isTextureCompressed);
//        if (layerName.toLowerCase().trim().equals("perimeter"))
//            layer.setName(layerName.toLowerCase());
//        //layerList.add(bunker3DLayer);
//
//        return bunker3DLayer;
//    }
//
//    protected Bunker3DLayer loadBunkerLayer(final BaseShapeObject baseShapeObject,
//                                            final String layerName,
//                                            final int resId,
//                                            final float extension,
//                                            final Layer.PointInterpolation interpolation, boolean isTextureCompressed) {
//        if (baseShapeObject == null)
//            return null;
//
//        Bunker3DLayer layer = null;
//        try {
//            layer = new Bunker3DLayer(layerName, baseShapeObject, context, resId, extension, interpolation, isTextureCompressed, (Course3DRenderer) this);
//            updateExtremePoints(layer);
//        } catch (Exception e) {
//            Course3DRenderer.getViewerLogger().error(e);
//            e.printStackTrace();
//        }
//        return layer;
//    }

    protected Layer loadLayer(final BaseShapeObject baseShapeObject,
                              final String layerName,
                              final int resId,
                              final float extension,
                              final Layer.PointInterpolation interpolation, boolean isTextureCompressed) {
        if (baseShapeObject == null)
            return null;

        Layer layer = null;
        try {
            layer = new Layer(layerName, baseShapeObject, context, resId, extension, interpolation, isTextureCompressed);
            updateExtremePoints(layer);
        } catch (Exception e) {
            Course3DRenderer.getViewerLogger().error(e);
        }
        return layer;
    }

    protected OutlineLayer layer = null;

    protected OutlineLayer addOutlineLayer(final BaseShapeObject baseShapeObject,
                                           final String layerName,
                                           final int resId,
                                           final float width,
                                           final Layer.PointInterpolation interpolation, boolean isTextureCompressed) {

        if (baseShapeObject == null)
            return null;

        try {
            layer = new OutlineLayer(layerName, baseShapeObject, context, resId, width, interpolation, isTextureCompressed);
            layer.setName(layerName + " outline");
            updateExtremePoints(layer);
            layerList.add(layer);
        } catch (Exception e) {
            getViewerLogger().error(e);
        }

        return layer;
    }

    protected double calculateViewOffset() {
        if (!elevationDataExist && !ElevationHelper.getInstance().tilesExist()) {
            return 0;
        }
        if (greencenter == null || greencenter.getFirstPointList() == null || greencenter.getFirstPointList().getFirstPoint() == null) {
            return 0;
        }
        double alt = ElevationHelper.getInstance().getHeightInPoint(greencenter.getFirstPointList().getFirstPoint(), (Course3DRenderer) this);

        return Math.abs(alt * Math.tan(VectorMath.deg2rad(currentViewAngle)));
    }

    protected double calculateViewOffsetInPoint(double x, double y, double angle) {
        if (!elevationDataExist && !ElevationHelper.getInstance().tilesExist()) {
            return 0;
        }
        if (greencenter == null || greencenter.getFirstPointList() == null || greencenter.getFirstPointList().getFirstPoint() == null) {
            return 0;
        }
        double alt = ElevationHelper.getInstance().getHeightInPoint(x, y, (Course3DRenderer) this);

        return Math.abs(alt * Math.tan(VectorMath.deg2rad(angle)));
    }

    public Vector getCameraProjectionPoint() {
        double cameraProjectionShift = currentZ * Math.cos(VectorMath.deg2rad(180 - 90 - currentViewAngle));
        double cameraProjectionPointX = (currentXPos + offsetX) + Math.cos(VectorMath.deg2rad(90 + currentRotationAngle)) * cameraProjectionShift;
        double cameraProjectionPointY = (currentYPos + offsetY) - Math.sin(VectorMath.deg2rad(90 + currentRotationAngle)) * cameraProjectionShift;
        return new Vector(-cameraProjectionPointX, -cameraProjectionPointY);
    }

    public double normalizeZ(double newValue) {

        zNormalizeArray.add(newValue);

        while (zNormalizeArray.size() > 5) {
            zNormalizeArray.remove(0);
        }

        double sum = 0;

        for (double val : zNormalizeArray) {
            sum += val;
        }

        return sum / zNormalizeArray.size();

    }

    protected double calculateMaxViewOffset() {

        return Math.abs((ElevationHelper.getInstance().mMaxHeight - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE) * Math.tan(VectorMath.deg2rad(currentViewAngle));
    }

    public void onOrientationChanged() {
        if (elevationDataExist && ElevationHelper.getInstance().tilesExist())
            Tile.releaseTexturesAndFrameBuffers();
    }

    public void onLocationOnScreenChanged(int[] locationOnScreen) {
        this.locationOnScreen = locationOnScreen;
    }

    protected void setTextureSet() {
        if (textureSet == null) {
            try {
                if (vectorGPSObject != null
                        && vectorGPSObject.getVectorGPSObject() != null
                        && vectorGPSObject.getVectorGPSObject().getBackground() != null
                        && vectorGPSObject.getVectorGPSObject().getBackground().getShapes() != null
                        && vectorGPSObject.getVectorGPSObject().getBackground().getShapes().getShape() != null
                        && vectorGPSObject.getVectorGPSObject().getBackground().getShapes().getShape().size() > 0
                        && vectorGPSObject.getVectorGPSObject().getBackground().getShapes().getShape().get(0).getAttributes() != null
                        && vectorGPSObject.getVectorGPSObject().getBackground().getShapes().getShape().get(0).getAttributes().getDescription() != null) {
                    if (vectorGPSObject.getVectorGPSObject().getBackground().getShapes().getShape().get(0).getAttributes().getDescription() == 1) {
                        textureSet = new DefaultTextureSet();
                        textureSetType = TextureSetType.DEFAULT;
                    } else {
                        textureSet = new DesertTextureSet();
                        textureSetType = TextureSetType.TROPICAL;
                    }
                } else {
                    textureSet = new DefaultTextureSet();
                    textureSetType = TextureSetType.DEFAULT;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Vector objectCoordinatesToScreenCoordinates(Vector position) {
        int[] viewport = new int[]{0, 0, viewportWidth, viewportHeight};
        float[] v = new float[4];
        GLU.gluProject((float) position.x, (float) position.y, (float) position.z, modelViewMatrix, 0, projectionMatrix, 0, viewport, 0, v, 0);
        return new Vector(v[0], viewportHeight - v[1], v[2]);
    }

    private void generateGreenRectangle(Vector... extremePoints) {
        int extremeLeft = (int) extremePoints[0].x;
        int extremeRight = (int) extremePoints[0].x;
        int extremeTop = (int) extremePoints[0].y;
        int extremeBottom = (int) extremePoints[0].y;
        for (int i = 0; i < extremePoints.length; i++) {
            extremeLeft = Math.min(extremeLeft, (int) extremePoints[i].x);
            extremeRight = Math.max(extremeRight, (int) extremePoints[i].x);
            extremeTop = Math.min(extremeTop, (int) extremePoints[i].y);
            extremeBottom = Math.max(extremeBottom, (int) extremePoints[i].y);
        }
        if (greenPositionChangeListener != null)
            greenPositionChangeListener.onPositionChanged(new Rect(extremeLeft, extremeTop, extremeRight, extremeBottom));
    }


    protected void calculateGreenPosition() {
        Vector v1 = new Vector(extremeGreenTop.x, extremeGreenTop.y, elevationDataExist && ElevationHelper.getInstance().tilesExist() ? ElevationHelper.getInstance().getHeightInPoint(extremeGreenTop, (Course3DRenderer) this) : 0);
        Vector v2 = new Vector(extremeGreenBottom.x, extremeGreenBottom.y, elevationDataExist && ElevationHelper.getInstance().tilesExist() ? ElevationHelper.getInstance().getHeightInPoint(extremeGreenBottom, (Course3DRenderer) this) : 0);
        Vector v3 = new Vector(extremeGreenLeft.x, extremeGreenLeft.y, elevationDataExist && ElevationHelper.getInstance().tilesExist() ? ElevationHelper.getInstance().getHeightInPoint(extremeGreenLeft, (Course3DRenderer) this) : 0);
        Vector v4 = new Vector(extremeGreenRight.x, extremeGreenRight.y, elevationDataExist && ElevationHelper.getInstance().tilesExist() ? ElevationHelper.getInstance().getHeightInPoint(extremeGreenRight, (Course3DRenderer) this) : 0);
        generateGreenRectangle(objectCoordinatesToScreenCoordinates(v1), objectCoordinatesToScreenCoordinates(v2), objectCoordinatesToScreenCoordinates(v3), objectCoordinatesToScreenCoordinates(v4));
    }

    public void setCurrentHole(final int holeNumber,
                               Course3DRendererBase.NavigationMode navigationMode,
                               boolean resetPosition,
                               Integer initialTeeBox) {

        HoleWithinCourse holeWithinCourse = new HoleWithinCourse();
        holeWithinCourse.setCourseId(courseId);
        holeWithinCourse.setHoleNumber(holeNumber);

        CourseGpsDetailsData data = null;

        if (courseGpsDetailsResponse != null) {
            data = (CourseGpsDetailsData) CollectionUtils.find(courseGpsDetailsResponse.getGPSList(), new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return ((CourseGpsDetailsData) object).getHoleNumber().equals(holeNumber);
                }
            });
        }

        frontLocation = null;
        backLocation = null;
        hazardList = null;
        if (data != null) {
            frontLocation = (data.getFrontLocation());
            backLocation = (data.getBackLocation());
            hazardList = data.getCustomPoints();
        }

        if (navigationMode == null && getNavigationMode() == null)
            setNavigationMode(Course3DRendererBase.DEFAULT_OVERALL_MODE);
        else if (navigationMode != null)
            setNavigationMode(navigationMode);
        if (frontLocation != null)
            setFrontGreeenLocationGPS(frontLocation);
        if (backLocation != null)
            setBackGreeenLocationGPS(backLocation);
        if (hazardList != null)
            setHazardList(hazardList);
        if (initialTeeBox == null)
            setHoleWithinCourse(holeWithinCourse, resetPosition);
        else
            setHoleWithinCourse(holeWithinCourse, resetPosition, initialTeeBox);

        needToClearGlBuffer = true;

        requestRender();
    }

    //NEW
    public void setCurrentHole(final int holeNumber,
                               boolean resetPosition,
                               Integer initialTeeBox) {

        HoleWithinCourse holeWithinCourse = new HoleWithinCourse();
        holeWithinCourse.setCourseId(courseId);
        holeWithinCourse.setHoleNumber(holeNumber);

        CourseGpsDetailsData data = (CourseGpsDetailsData) CollectionUtils.find(courseGpsDetailsResponse.getGPSList(), new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((CourseGpsDetailsData) object).getHoleNumber().equals(holeNumber);
            }
        });

        frontLocation = null;
        backLocation = null;
        hazardList = null;
        if (data != null) {
            frontLocation = (data.getFrontLocation());
            backLocation = (data.getBackLocation());
            hazardList = data.getCustomPoints();
        }

        if (getNavigationMode() == null)
            setNavigationMode(Course3DRendererBase.DEFAULT_OVERALL_MODE);
        if (initialTeeBox == null)
            setHoleWithinCourse(holeWithinCourse, resetPosition);
        else
            setHoleWithinCourse(holeWithinCourse, resetPosition);
        if (frontLocation != null)
            setFrontGreeenLocationGPS(frontLocation);
        if (backLocation != null)
            setBackGreeenLocationGPS(backLocation);
        if (hazardList != null)
            setHazardList(hazardList);

        requestRender();

    }

    protected Location getFlagLocation() {
        Location retVal = new Location("");
        try {
            Location overridedPinPosition = getOverridedPinPosition();
            if (overridedPinPosition == null) {
                hasPinOverride = false;

                final float flagX = (float) greencenter.getPointList().get(0).getPointList()[0].x;
                final float flagY = (float) greencenter.getPointList().get(0).getPointList()[0].y;
                retVal = new Location("");
                retVal.setLongitude(flagX);
                retVal.setLatitude(flagY);

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
                retVal = new Location("");
                retVal.setLongitude(flagX);
                retVal.setLatitude(flagY);
                flagLocationGPS = overridedPinPosition;
            }
        } catch (Exception e) {

        }
        return retVal;
    }

    public void updateCart(Integer idCart, String cartName, Location cartLocation) {
        if (cartDrawData != null)
            cartDrawData.updateCart(idCart, cartName, cartLocation);
    }

    public void removeCart(Integer idCart) {
        if (cartDrawData != null)
            cartDrawData.removeCart(idCart);
    }
}
