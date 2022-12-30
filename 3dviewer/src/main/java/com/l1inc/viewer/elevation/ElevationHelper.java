package com.l1inc.viewer.elevation;

import android.support.annotation.Keep;
import android.util.Log;

import com.google.gson.Gson;
import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.drawing.Constants;
import com.l1inc.viewer.drawing.Layer;
import com.l1inc.viewer.drawing.TextureCache;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;
import com.l1inc.viewer.parcer.ElevationDataObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Kirill Kartukov on 20.11.2017.
 */

public class ElevationHelper {

    private static volatile ElevationHelper instance;

    public Float mMinHeight;
    public Float mMaxHeight;
    public int mapWidth;
    public int mapHeight;
    private List<Float> vertexList = new ArrayList<>();
    private int tileHeightInPoints = 0;
    private int tileWidthInPoints = 0;

    private int tileYCount = 0;
    private int tileXCount = 0;
    private int pointsInTile = 9;

    private final int[] rotationAngles = new int[]{0, 45, 90, 135, 180, 225, 270, 315, 360};

    private ArrayList<Tile> tileList = new ArrayList<>();
    private Vector point = new Vector();
    private Vector bottomLeft = new Vector();
    private Vector topLeft = new Vector();
    private Vector bottomRight = new Vector();
    private Vector topRight = new Vector();


    public Map<Integer, Integer> temp = new HashMap<>();
    private ArrayList<DrawTileHelper> texturesToUse = new ArrayList<>();
    private Map<Integer, Integer> drawTiles = new HashMap<>();
    private ArrayList<ArrayList<Vector>> vectorArrayList = new ArrayList<>();
    public ArrayList<Vector> vectorArrayList1D = new ArrayList<>();

    private ArrayList<Vector> vectorArrayList2d = new ArrayList<>();

    private Gson gson = new Gson();

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    private ElevationDataObject elevationDataObject;


    private boolean elevationDataLoaded = false;

    private ElevationDataLoadingListener elevationDataLoadingListener;

    public boolean isElevationDataLoaded() {
        return elevationDataLoaded;
    }


    public static ElevationHelper getInstance() {
        if (null == instance) {
            instance = new ElevationHelper();
        }
        return instance;
    }
    @Keep
    public boolean parseElevationData(String elevationString, ElevationDataLoadingListener listener) {

        if (listener != null) {
            Layer.resetBasePoints();
            ElevationHelper.getInstance().prepare();
            this.elevationDataLoadingListener = listener;
        }

        if (elevationString == null || elevationString.trim().length() == 0) {
            if (elevationDataLoadingListener != null)
                elevationDataLoadingListener.onElevationDataLoadingFail();
            elevationDataLoadingListener = null;
            return false;
        }
        try {
            elevationDataObject = gson.fromJson(elevationString, ElevationDataObject.class);
        } catch (Exception e) {
            if (elevationDataLoadingListener != null)
                elevationDataLoadingListener.onElevationDataLoadingFail();
            e.printStackTrace();
            elevationDataLoadingListener = null;
            return false;
        }

        if (elevationDataObject == null || elevationDataObject.getmElevationStep() == null || elevationDataObject.getmElevationMaxLatitude() == null || elevationDataObject.getmElevationMaxLatitude() == null) {
            if (elevationDataLoadingListener != null)
                elevationDataLoadingListener.onElevationDataLoadingFail();
            elevationDataLoadingListener = null;
            return false;
        }

        if (elevationDataObject.getmElevationArray() == null || elevationDataObject.getmElevationArray().size() == 0 || elevationDataObject.getmElevationArray().get(0).size() == 0) {
            if (elevationDataLoadingListener != null)
                elevationDataLoadingListener.onElevationDataLoadingFail();
            elevationDataLoadingListener = null;
            return false;
        }

        mMinHeight = Collections.min(elevationDataObject.getmElevationArray().get(0));
        mMaxHeight = mMinHeight;
        for (int i = 1; i < elevationDataObject.getmElevationArray().size(); i++) {
            vectorArrayList.add(new ArrayList<Vector>());
            mMinHeight = Math.min(mMinHeight, Collections.min(elevationDataObject.getmElevationArray().get(i)));
            mMaxHeight = Math.max(mMaxHeight, Collections.max(elevationDataObject.getmElevationArray().get(i)));
        }

        //ADDING TOP HEIGHT TO ZERO - LINE\

        elevationDataObject.setmElevationMaxLatitude(elevationDataObject.getmElevationMaxLatitude() + elevationDataObject.getmElevationStep());
        elevationDataObject.setmElevationMinLongitude(elevationDataObject.getmElevationMinLongitude() - elevationDataObject.getmElevationStep());

        int arraySize = elevationDataObject.getmElevationArray().size();


        for (int i = 0; i < arraySize; i++) {
            elevationDataObject.getmElevationArray().get(i).add(0, mMinHeight);
            elevationDataObject.getmElevationArray().get(i).add(mMinHeight);
        }

        ArrayList<Float> toZeroRow = new ArrayList<>(Collections.nCopies(elevationDataObject.getmElevationArray().get(0).size(), mMinHeight));

        elevationDataObject.getmElevationArray().add(0, toZeroRow);
        elevationDataObject.getmElevationArray().add(elevationDataObject.getmElevationArray().size(), toZeroRow);

        mapWidth = elevationDataObject.getmElevationArray().get(0).size();
        mapHeight = elevationDataObject.getmElevationArray().size();
        if (elevationDataLoadingListener != null)
            return splitToTiles();
        return true;
    }

    private void addLog(String mes) {
        Log.e("ElevationHelper", mes);
    }

    private ArrayList<Vector> vectorsList = new ArrayList<>();

    public boolean splitToTiles() {

        try {

            float xC;
            float yC;
            float zC;

            int tileNumberX = 0;
            int tileNumberY = 0;

            double tilesXCount = Math.ceil((mapWidth - 1) / (pointsInTile - 1));
            double tilesYCount = Math.ceil((mapHeight - 1) / (pointsInTile - 1));

            tileYCount = (int) (tilesYCount);
            tileXCount = (int) (tilesXCount);


            tileList.clear();
            tileList = new ArrayList<>((int) tilesYCount);
            vectorsList.clear();
            Tile drawTile;
            Vector retVal;

            int startRow = 0;
            int endRow = 0;
            int startPos = 0;
            int endPos = 0;

            int currentTilesCountInNode = 0;
            int tileNumber = 0;

            int currentNode = 0;
            double tilesInNode = 4.0;
            int nodeXCount = (int) Math.ceil((double) tileXCount / tilesInNode);
            int nodeYCount = (int) Math.ceil((double) tileYCount / tilesInNode);
            int totalNodeCount = nodeXCount * nodeYCount;
            int nodeHeight = 1;

            while (tileNumberY < tilesYCount) {
                while (tileNumberX < tilesXCount) {
                    vectorsList.clear();
                    vertexList.clear();
                    tileHeightInPoints = 0;
                    int fromYv = Math.max(tileNumberY * pointsInTile - tileNumberY, 0);
                    int toYv = Math.min(mapHeight, (Math.max(tileNumberY * pointsInTile - tileNumberY, 0) + pointsInTile));
                    startRow = fromYv;
                    endRow = toYv;
                    for (int y = fromYv; y < toYv; y++) {
                        tileWidthInPoints = 0;
                        int fromV = Math.max(tileNumberX * pointsInTile - tileNumberX, 0);
                        int toV = Math.min(mapWidth, (Math.max(tileNumberX * pointsInTile - tileNumberX, 0)) + pointsInTile);
                        startPos = fromV;
                        endPos = toV;
                        int size = toV - fromV;
                        for (int x = fromV; x < toV; x++) {
                            yC = (float) Layer.transformLat((elevationDataObject.getmElevationMaxLatitude() - elevationDataObject.getmElevationStep() * (float) y));
                            xC = (float) Layer.transformLon((elevationDataObject.getmElevationMinLongitude() + elevationDataObject.getmElevationStep() * (float) x));
                            zC = (elevationDataObject.getmElevationArray().get(y).get(x) - mMinHeight) / Constants.LOCATION_SCALE;

                            retVal = null;
                            retVal = new Vector(xC, yC, zC);
                            vectorsList.add(retVal);
                            vertexList.add(xC);
                            vertexList.add(yC);
                            vertexList.add(zC);
                            tileWidthInPoints++;

                        }

                        tileHeightInPoints++;
                    }

                    if (!TileStaticData.getInstance().uvListExist(tileHeightInPoints, tileWidthInPoints)) {
                        TileStaticData.getInstance().uvList = new ArrayList<>();
                        for (int y = 0; y < tileHeightInPoints; y++) {
                            for (int x = 0; x < tileWidthInPoints; x++) {
                                TileStaticData.getInstance().uvList.add((float) y / ((float) (tileHeightInPoints - 1)));
                                TileStaticData.getInstance().uvList.add((float) ((tileWidthInPoints - 1) - x) / ((float) (tileWidthInPoints - 1)));
                            }
                        }
                        TileStaticData.getInstance().byteBuf = ByteBuffer.allocateDirect(TileStaticData.getInstance().uvList.size() * 4);
                        TileStaticData.getInstance().byteBuf.order(ByteOrder.nativeOrder());
                        TileStaticData.getInstance().textureBuffer = TileStaticData.getInstance().byteBuf.asFloatBuffer();
                        Collections.reverse(TileStaticData.getInstance().uvList);
                        for (Float val : TileStaticData.getInstance().uvList) {
                            TileStaticData.getInstance().textureBuffer.put(val);
                        }
                        TileStaticData.getInstance().textureBuffer.position(0);
                        TileStaticData.getInstance().addUVList(tileHeightInPoints, tileWidthInPoints);

                        TileStaticData.getInstance().uvList.clear();
                        TileStaticData.getInstance().byteBuf.clear();
                        TileStaticData.getInstance().textureBuffer.clear();
                    }
                    drawTile = new Tile(vertexList, vectorsList, tileWidthInPoints, tileHeightInPoints, startPos, startRow, endPos, endRow);
                    vertexList.clear();
                    vectorsList.clear();
                    tileList.add(drawTile);
                    tileNumberX += 1;

               /* if (currentTilesCountInNode < tilesInNode) {
                    if(nodesArray.size()<currentNode+1){
                        nodesArray.add(currentNode, new TileQuadNode());
                    }
                    nodesArray.get(currentNode).addTileIndex(tileNumber, currentNode);
                    currentTilesCountInNode++;
                } else {
                    currentNode++;
                    currentTilesCountInNode = 1;
                    if(nodesArray.size()<currentNode+1){
                        nodesArray.add(currentNode, new TileQuadNode());
                    }
                    nodesArray.get(currentNode).addTileIndex(tileNumber, currentNode);
                }*/

                    tileNumber++;

                }

                currentTilesCountInNode = 0;
                tileNumberX = 0;
                tileNumberY += 1;
            /*if (tileNumberY % tilesInNode == 0) {
                currentNode++;
                nodeHeight++;
            } else {
                currentNode = currentNode+1-nodeXCount;
                nodeHeight = 1;
                nodeHeight++;
            }*/

            }
            vectorArrayList.clear();

            double maxLatitude = ElevationHelper.getInstance().getMaxLatitude();
            double minLongitude = ElevationHelper.getInstance().getMinLongitude();
            double step = ElevationHelper.getInstance().getStep();
            float minHeight = ElevationHelper.getInstance().mMinHeight;
            vectorArrayList1D = new ArrayList<>();
            for (int y = 0; y < mapHeight; y++) {
                ArrayList<Vector> vR = new ArrayList<>();
                for (int x = 0; x < mapWidth; x++) {
                    yC = (float) Layer.transformLat((maxLatitude - step * (float) y));
                    xC = (float) Layer.transformLon((minLongitude + step * (float) x));
                    zC = (elevationDataObject.getmElevationArray().
                            get(y).
                            get(x) - mMinHeight) / Constants.LOCATION_SCALE;
                    retVal = null;
                    retVal = new Vector(xC, yC, zC);
                    vR.add(retVal);
                    vectorArrayList1D.add(retVal);
                }
                vectorArrayList.add(vR);
            }


            Vector normal;

            int vertIndex = 0;

            for (int i = 0; i <= mapHeight - 2; i++) {
                for (int j = 0; j <= mapWidth - 2; j++) {
                    int t = j + i * mapWidth;

                    vertIndex++;
                    vertIndex++;
                    vertIndex++;

                    normal = VectorMath.getNormals2(vectorArrayList1D.get(t + mapWidth + 1), vectorArrayList1D.get(t + 1), vectorArrayList1D.get(t));

                    vectorArrayList1D.get(t + mapWidth + 1).setNormals(normal.x, normal.y, normal.z);
                    vectorArrayList1D.get(t + 1).setNormals(normal.x, normal.y, normal.z);
                    vectorArrayList1D.get(t).setNormals(normal.x, normal.y, normal.z);

                    vertIndex++;
                    vertIndex++;
                    vertIndex++;

                    normal = VectorMath.getNormals2(vectorArrayList1D.get(t + mapWidth), vectorArrayList1D.get(t + mapWidth + 1), vectorArrayList1D.get(t));

                    vectorArrayList1D.get(t + mapWidth).setNormals(normal.x, normal.y, normal.z);
                    vectorArrayList1D.get(t + mapWidth + 1).setNormals(normal.x, normal.y, normal.z);
                    vectorArrayList1D.get(t).setNormals(normal.x, normal.y, normal.z);

                }
            }
            for (Tile tile : tileList) {
                tile.initNormals();
            }


            if (elevationDataLoadingListener != null) {
                elevationDataLoadingListener.onElevationDataLoadingSuccess();
                elevationDataLoadingListener = null;
            }

            elevationDataLoaded = true;

        } catch (Exception e) {
            reset();
            elevationDataObject = null;
            e.printStackTrace();
            if (elevationDataLoadingListener != null) {
                elevationDataLoadingListener.onElevationDataLoadingFail();
                elevationDataLoadingListener = null;
            }
            return false;
        } catch (OutOfMemoryError e) {
            reset();
            elevationDataObject = null;
            e.printStackTrace();
            if (elevationDataLoadingListener != null) {
                elevationDataLoadingListener.onElevationDataLoadingFail();
                elevationDataLoadingListener = null;
            }
            return false;
        }

        return true;
    }


    public ArrayList<Vector> getNormalsArrayByPosition(int startX, int startY, int endX, int endY) {
        ArrayList<Vector> retval = new ArrayList<>();
        while (startY < endY) {
            retval.addAll(new ArrayList<>(vectorArrayList1D.subList(getIndexByXY(startY, startX), getIndexByXY(startY, endX))));
            startY++;
        }

        return retval;
    }

    private int getIndexByXY(int y, int x) {
        return y * mapWidth + x;
    }

    public void setCurrentTileByCameraPosition(Course3DRenderer renderer) {
        if (!tilesExist())
            return;

        int indexX;
        int indexY;

        indexX = (int) Math.floor((Layer.transformToLon(-renderer.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexY = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(-renderer.y)) / elevationDataObject.getmElevationStep());

        int x;
        int y;

        x = (int) Math.floor((indexX - 1) / (pointsInTile - 1));
        y = (int) Math.floor((indexY - 1) / (pointsInTile - 1));

        if (y >= 0 && y < tileYCount && x >= 0 && x < tileXCount)
            setCurrentTile(x, y, getRotationAngle((int) (renderer.rotationAngle < 0 ? 360f + renderer.rotationAngle : renderer.rotationAngle)), renderer);

    }

    public void setCurrentTileByCameraPositionAndShift(Course3DRenderer renderer) {
        if (!tilesExist())
            return;

        int indexX;
        int indexY;

        float realX = (float) (renderer.x - Math.cos(VectorMath.deg2rad(90 + renderer.currentRotationAngle)) * renderer.viewShift);
        float realY = (float) (renderer.y + Math.sin(VectorMath.deg2rad(90 + renderer.currentRotationAngle)) * renderer.viewShift);

        indexX = (int) Math.floor((Layer.transformToLon(-realX) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexY = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(-realY)) / elevationDataObject.getmElevationStep());

        int x;
        int y;

        x = (int) Math.floor((indexX - 1) / (pointsInTile - 1));
        y = (int) Math.floor((indexY - 1) / (pointsInTile - 1));

        if (y >= 0 && y < tileYCount && x >= 0 && x < tileXCount)
            setCurrentTile(x, y, getRotationAngle((int) (renderer.rotationAngle < 0 ? 360f + renderer.rotationAngle : renderer.rotationAngle)), renderer);

    }

    public void setCurrentTileByFlyoverPosition(float xPos, float yPos, Course3DRenderer renderer) {
        if (!tilesExist())
            return;

        int indexX;
        int indexY;

        float realX = (float) (xPos - Math.cos(VectorMath.deg2rad(90 + renderer.currentRotationAngle)) * renderer.viewShift);
        float realY = (float) (yPos + Math.sin(VectorMath.deg2rad(90 + renderer.currentRotationAngle)) * renderer.viewShift);

        indexX = (int) Math.floor((Layer.transformToLon(-realX) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexY = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(-realY)) / elevationDataObject.getmElevationStep());

        int x;
        int y;

        x = (int) Math.floor((indexX - 1) / (pointsInTile - 1));
        y = (int) Math.floor((indexY - 1) / (pointsInTile - 1));

        if (y >= 0 && y < tileYCount && x >= 0 && x < tileXCount)
            setCurrentTile(x, y, getRotationAngle((int) (renderer.rotationAngle < 0 ? 360f + renderer.rotationAngle : renderer.rotationAngle)), renderer);

    }

    public void renderPerimeterHole(Course3DRenderer renderer, Vector leftTop, Vector rightTop, Vector leftBottom, Vector rightBottom) {
        if (!tilesExist())
            return;

        tilesWithPossibleTextures.clear();
        if (elevationDataObject.getmElevationArray() == null ||
                elevationDataObject.getmElevationArray().size() == 0 ||
                elevationDataObject.getmElevationArray().get(0).size() == 0 ||
                elevationDataObject.getmElevationMaxLatitude() == null ||
                elevationDataObject.getmElevationMinLongitude() == null ||
                elevationDataObject.getmElevationStep() == null) {
            return;
        }

        int indexXlt;
        int indexYlt;
        int indexXrt;
        int indexYrt;
        int indexXlb;
        int indexYlb;
        int indexXrb;
        int indexYrb;

        indexXlt = (int) Math.floor((Layer.transformToLon(leftTop.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYlt = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(leftTop.y)) / elevationDataObject.getmElevationStep());
        indexXrt = (int) Math.floor((Layer.transformToLon(rightTop.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYrt = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(rightTop.y)) / elevationDataObject.getmElevationStep());
        indexXlb = (int) Math.floor((Layer.transformToLon(leftBottom.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYlb = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(leftBottom.y)) / elevationDataObject.getmElevationStep());
        indexXrb = (int) Math.floor((Layer.transformToLon(rightBottom.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYrb = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(rightBottom.y)) / elevationDataObject.getmElevationStep());

        int xLT;
        int yLT;
        int yRT;
        int xRT;
        int yLB;
        int xLB;
        int xRB;
        int yRB;

        xLT = (int) Math.floor((indexXlt - 1) / (pointsInTile - 1));
        yLT = (int) Math.floor((indexYlt - 1) / (pointsInTile - 1));

        xRT = (int) Math.floor((indexXrt - 1) / (pointsInTile - 1));
        yRT = (int) Math.floor((indexYrt - 1) / (pointsInTile - 1));

        xLB = (int) Math.floor((indexXlb - 1) / (pointsInTile - 1));
        yLB = (int) Math.floor((indexYlb - 1) / (pointsInTile - 1));

        xRB = (int) Math.floor((indexXrb - 1) / (pointsInTile - 1));
        yRB = (int) Math.floor((indexYrb - 1) / (pointsInTile - 1));

        int leftTopTileIndex = getTileIndex(xLT, yLT);
        int rightTopTileIndex = getTileIndex(xRT, yRT);
        int leftBottomTileIndex = getTileIndex(xLB, yLB);
        int rightBottomTileIndex = getTileIndex(xRB, yRB);


        int firstTileInRow = leftTopTileIndex;
        for (int i = 0; i <= (leftBottomTileIndex - leftTopTileIndex) / tileXCount; i++) {
            for (int j = 0; j <= rightTopTileIndex - leftTopTileIndex; j++) {
                tilesWithPossibleTextures.add((firstTileInRow + j));
            }
            firstTileInRow += tileXCount;
        }


    }

    private ArrayList<Integer> tilesWithPossibleTextures = new ArrayList<>();

    public void setStartLayerIndexes(Vector leftTop, Vector rightTop, Vector leftBottom, Vector rightBottom) {
        tilesWithPossibleTextures.clear();
        if (!tilesExist())
            return;

        int indexXlt;
        int indexYlt;
        int indexXrt;
        int indexYrt;
        int indexXlb;
        int indexYlb;
        int indexXrb;
        int indexYrb;

        indexXlt = (int) Math.floor((Layer.transformToLon(leftTop.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYlt = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(leftTop.y)) / elevationDataObject.getmElevationStep());
        indexXrt = (int) Math.floor((Layer.transformToLon(rightTop.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYrt = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(rightTop.y)) / elevationDataObject.getmElevationStep());
        indexXlb = (int) Math.floor((Layer.transformToLon(leftBottom.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYlb = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(leftBottom.y)) / elevationDataObject.getmElevationStep());
        indexXrb = (int) Math.floor((Layer.transformToLon(rightBottom.x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        indexYrb = (int) Math.floor((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(rightBottom.y)) / elevationDataObject.getmElevationStep());

        int xLT;
        int yLT;
        int yRT;
        int xRT;
        int yLB;
        int xLB;
        int xRB;
        int yRB;

        xLT = (int) Math.floor((indexXlt - 1) / (pointsInTile - 1));
        yLT = (int) Math.floor((indexYlt - 1) / (pointsInTile - 1));

        xRT = (int) Math.floor((indexXrt - 1) / (pointsInTile - 1));
        yRT = (int) Math.floor((indexYrt - 1) / (pointsInTile - 1));

        xLB = (int) Math.floor((indexXlb - 1) / (pointsInTile - 1));
        yLB = (int) Math.floor((indexYlb - 1) / (pointsInTile - 1));

        xRB = (int) Math.floor((indexXrb - 1) / (pointsInTile - 1));
        yRB = (int) Math.floor((indexYrb - 1) / (pointsInTile - 1));

        int leftTopTileIndex = getTileIndex(xLT, yLT);
        int rightTopTileIndex = getTileIndex(xRT, yRT);
        int leftBottomTileIndex = getTileIndex(xLB, yLB);
        int rightBottomTileIndex = getTileIndex(xRB, yRB);

        int firstTileInRow = leftTopTileIndex;
        for (int i = 0; i <= (leftBottomTileIndex - leftTopTileIndex) / tileXCount; i++) {
            for (int j = 0; j <= rightTopTileIndex - leftTopTileIndex; j++) {
                tilesWithPossibleTextures.add((firstTileInRow + j));
            }
            firstTileInRow += tileXCount;
        }

    }


    private int getRotationAngle(int rotationAngle) {
        float distance = Math.abs(rotationAngles[0] - rotationAngle);
        int idx = 0;
        for (int c = 1; c < rotationAngles.length; c++) {
            float cdistance = Math.abs(rotationAngles[c] - rotationAngle);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        if (idx == rotationAngles.length - 1)
            idx = 0;
        return rotationAngles[idx];
    }

    private int currentTileX = -1;
    private int currentTileY = -1;

    public void resetCurrentTiles() {
        currentTileX = -1;
        currentTileY = -1;
    }

    private void setCurrentTile(int x, int y, int rotationAngle, Course3DRenderer renderer) {
/*
        if (currentTileX == x && currentTileY == y)
            return;*/

        currentTileX = x;
        currentTileY = y;
        updateTileTextureQuality(renderer, rotationAngle);
    }

    private int getTileIndex(int x, int y) {
        return tileXCount * y + x;
    }

    public void getVectorByPosition(int x, int y) {

    }

    public void clearVBO() {
        if (drawTiles != null && tilesExist())
            for (int key : drawTiles.keySet()) {
                tileList.get(key).setTextureQuality(TileStaticData.getInstance().TEXTURE_QUALITY_NONE, key);
            }
    }

    public boolean tilesExist() {
        return !(elevationDataObject == null ||
                elevationDataObject.getmElevationArray() == null ||
                elevationDataObject.getmElevationArray().size() == 0 ||
                elevationDataObject.getmElevationArray().get(0).size() == 0 ||
                elevationDataObject.getmElevationMaxLatitude() == null ||
                elevationDataObject.getmElevationMinLongitude() == null ||
                elevationDataObject.getmElevationStep() == null);
    }


    public void updateTileTextureQuality(Course3DRenderer renderer, int rotationAngle) {

        int tileIndex = getTileIndex(currentTileX, currentTileY);

        temp.clear();
        getUsedTextures(tileIndex, rotationAngle);
        for (DrawTileHelper dh : texturesToUse) {
            if (drawTiles.get(dh.getPosition()) != null) {
                temp.put(dh.getPosition(), dh.getTextureQuality());
                drawTiles.remove(dh.getPosition());
            } else {
                temp.put(dh.getPosition(), dh.getTextureQuality());
            }
        }

        for (int key : drawTiles.keySet()) {
            tileList.get(key).setTextureQuality(0, key);
        }

        drawTiles.clear();
        drawTiles.putAll(temp);
        temp.clear();

        if (texturesToUse.size() < 1)
            return;

        for (DrawTileHelper dh : texturesToUse) {
            drawTiles.put(dh.getPosition(), dh.getTextureQuality());
        }

        texturesToUse.clear();



    }

    private void getUsedTextures(int centerTile, int rotationAngle) {
        if (texturesToUse == null)
            texturesToUse = new ArrayList<>();
        texturesToUse.clear();

        int tilesInRow;
        int tilesRowLeft = 5;
        int[] startEndPos = getTileStartPosEndPosByRotationAngle(centerTile, rotationAngle);
        int tileStartPos = startEndPos[0];
        int tileEndPos = startEndPos[1];
        tilesInRow = startEndPos[2];
        int tilePosInRow = 0;
        //TODO THIS CAUSES BUGS ON PRESIDIO RIVERWALK
        //tileStartPos = tileStartPos < 0 ? 0 : tileStartPos;
        //tileEndPos = tileEndPos > tileList.size()-1 ? tileList.size()-1 : tileEndPos;
        while (tileStartPos < tileEndPos) {
            while (tilePosInRow < tilesInRow) {
                Tile tile = getTileByPosition(tileStartPos);
                if (tileStartPos > -1 && tileStartPos < tileList.size() && tileContainsLayers(tileStartPos) && tile != null && Frustum.cubeInFrustum(tile))
                    texturesToUse.add(new DrawTileHelper(tileStartPos, TileStaticData.TEXTURE_QUALITY_HIGH));
                tilePosInRow++;

                tileStartPos++;
            }
            tilePosInRow = 0;
            tileStartPos = tileStartPos + tileXCount - tilesInRow;
            tilesRowLeft--;


        }

    }

    private boolean isHighQuality(int pos, int center) {
        return pos == center - 1 || pos == center + 1 || pos == center || pos == center + tileXCount || pos == center + tileXCount - 1 || pos == center + tileXCount + 1 || pos == center - tileXCount || pos == center - tileXCount - 1 || pos == center - tileXCount + 1;
    }

    private boolean tileContainsLayers(int pos) {
        if (tilesWithPossibleTextures == null || tilesWithPossibleTextures.size() == 0)
            return false;
        return tilesWithPossibleTextures.indexOf(pos) > -1;
    }

    private int[] getTileStartPosEndPosByRotationAngle(int centerTile, int rotationAngle) {
        int minTileXindexInStartRow = 0;
        int maxTileIndexInLastRow = 0;
        int startPos;
        int endPos;
        int tileOffset = 5;
        int tilesInRow = tileOffset * 2 + 1;

        minTileXindexInStartRow = getTileIndex(0, currentTileY - tileOffset);
        maxTileIndexInLastRow = getTileIndex(tileXCount - 1, currentTileY + tileOffset);
        startPos = centerTile - tileXCount * tileOffset - tileOffset;
        endPos = centerTile + tileXCount * tileOffset + tileOffset;
        while (startPos < minTileXindexInStartRow) {
            startPos++;
            tilesInRow--;
        }
        while (startPos > maxTileIndexInLastRow) {
            startPos--;
            tilesInRow--;
        }
        return new int[]{startPos, endPos, tilesInRow};

    }


    public Map<Integer, Integer> getDrawTiles() {
        return drawTiles;
    }

    public boolean drawDrawTiles(Course3DRenderer renderer) {
        try {
            synchronized (drawTiles) {

                int drawTileCount = 0;
                for (int key : drawTiles.keySet()) {
                    if (tileList.get(key) != null) {
                        boolean isTextureExists = tileList.get(key).draw(renderer, drawTileCount);
                        if(!isTextureExists)
                            return false;
                        drawTileCount++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }

    public synchronized void clearDrawTiles() {

        synchronized (drawTiles) {
            for (int key : drawTiles.keySet())
                tileList.get(key).destroy();
            drawTiles.clear();
        }

    }

    public float getHeightInPoint(Vector point, Course3DRenderer renderer) {
        return getHeightInPoint((float) point.x, (float) point.y, renderer, false);
    }

    public float getHeightInPoint(Vector point, Course3DRenderer renderer, boolean returnMinusHeightOnError) {
        return getHeightInPoint((float) point.x, (float) point.y, renderer, returnMinusHeightOnError);
    }

    public float getHeightInPoint(double x, double y, Course3DRenderer renderer) {
        return getHeightInPoint((float) x, (float) y, renderer, false);
    }

    private float retVal;

    public float getHeightInPoint(float x, float y, Course3DRenderer renderer, boolean returnMinusHeightOnError) {
        //REMOVED DETECTING OF 2D VIEW BECAUSE OF INCORRECT Z_POSITION RENDERING FOR TREES AFTER 2D MODE IS TURNED OFF
        if (!tilesExist()) {
            return 0;
        }

        retVal = returnMinusHeightOnError ? -1 : (mMaxHeight - mMinHeight) / Constants.LOCATION_SCALE;

        int bottomLeftIndexX = (int) ((Layer.transformToLon(x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        int bottomLeftIndexY = (int) Math.ceil((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(y)) / elevationDataObject.getmElevationStep());

        int topLeftIndexX = bottomLeftIndexX;
        int topLeftIndexY = bottomLeftIndexY - 1;

        int bottomRightIndexX = bottomLeftIndexX + 1;
        int bottomRightIndexY = bottomLeftIndexY;

        int topRightIndexX = bottomLeftIndexX + 1;
        int topRightIndexY = bottomLeftIndexY - 1;

        double bottomLatitude = elevationDataObject.getmElevationMaxLatitude() - elevationDataObject.getmElevationStep() * bottomLeftIndexY;
        double topLatitude = elevationDataObject.getmElevationMaxLatitude() - elevationDataObject.getmElevationStep() * (bottomLeftIndexY - 1);

        double leftLongitude = elevationDataObject.getmElevationMinLongitude() + elevationDataObject.getmElevationStep() * bottomLeftIndexX;
        double rightLongitude = elevationDataObject.getmElevationMinLongitude() + elevationDataObject.getmElevationStep() * (bottomLeftIndexX + 1);

        double bottomY = Layer.transformLat(bottomLatitude);
        double topY = Layer.transformLat(topLatitude);

        double leftX = Layer.transformLon(leftLongitude);
        double rightX = Layer.transformLon(rightLongitude);

        if (bottomLeftIndexY < 1 || bottomLeftIndexY > elevationDataObject.getmElevationArray().size() - 2 || bottomLeftIndexX < 1 || bottomLeftIndexX > elevationDataObject.getmElevationArray().get(bottomLeftIndexY).size() - 2) {
            return retVal;
        }

        if (topLeftIndexY < 1 || topLeftIndexY > elevationDataObject.getmElevationArray().size() - 2 || topLeftIndexX < 1 || topLeftIndexX > elevationDataObject.getmElevationArray().get(topLeftIndexY).size() - 2) {
            return retVal;
        }

        if (bottomRightIndexY < 1 || bottomRightIndexY > elevationDataObject.getmElevationArray().size() - 2 || bottomRightIndexX < 1 || bottomRightIndexX > elevationDataObject.getmElevationArray().get(bottomRightIndexY).size() - 2) {
            return retVal;
        }

        if (topRightIndexY < 1 || topRightIndexY > elevationDataObject.getmElevationArray().size() - 2 || topRightIndexX < 1 || topRightIndexX > elevationDataObject.getmElevationArray().get(topRightIndexY).size() - 2) {
            return retVal;
        }

        double bottomLeftZ = (elevationDataObject.getmElevationArray().get(bottomLeftIndexY).get(bottomLeftIndexX) - mMinHeight) / Constants.LOCATION_SCALE;
        double topLeftZ = (elevationDataObject.getmElevationArray().get(topLeftIndexY).get(topLeftIndexX) - mMinHeight) / Constants.LOCATION_SCALE;
        double bottomRightZ = (elevationDataObject.getmElevationArray().get(bottomRightIndexY).get(bottomRightIndexX) - mMinHeight) / Constants.LOCATION_SCALE;
        double topRightZ = (elevationDataObject.getmElevationArray().get(topRightIndexY).get(topRightIndexX) - mMinHeight) / Constants.LOCATION_SCALE;

        updateVector(point, x, y, 0f);
        updateVector(bottomLeft, leftX, bottomY, bottomLeftZ);
        updateVector(topLeft, leftX, topY, topLeftZ);
        updateVector(bottomRight, rightX, bottomY, bottomRightZ);
        updateVector(topRight, rightX, topY, topRightZ);

        double distanceToBottomLeft = VectorMath.distance(point, bottomLeft);
        double distanceToTopRight = VectorMath.distance(point, topRight);

        return VectorMath.calculateZPositionForPoint(x, y, topLeft, bottomRight, distanceToBottomLeft > distanceToTopRight ? topRight : bottomLeft);
    }

    private void updateVector(Vector v, float x, float y, float z) {
        v.x = x;
        v.y = y;
        v.z = z;
    }

    private void updateVector(Vector v, double x, double y, double z) {
        v.x = x;
        v.y = y;
        v.z = z;
    }

    public Tile getTileByPosition(int pos) {
        if (tileList == null || pos >= tileList.size() || pos < 0)
            return null;
        return tileList.get(pos);
    }

    public ArrayList<Tile> getTileList() {
        return tileList;
    }

    public ArrayList<ArrayList<Float>> getElevationArray() {
        if (!tilesExist())
            return null;
        return elevationDataObject.getmElevationArray();
    }

    public int getMapWidth() {
        if (!tilesExist())
            return 0;
        return elevationDataObject.getmElevationArray().get(0).size();
    }

    public int getMapHeight() {
        if (!tilesExist())
            return 0;
        return elevationDataObject.getmElevationArray().size();
    }

    public Double getMaxLatitude() {
        if (!tilesExist())
            return 0d;
        return elevationDataObject.getmElevationMaxLatitude();
    }

    public Double getMinLongitude() {
        if (!tilesExist())
            return 0d;
        return elevationDataObject.getmElevationMinLongitude();
    }

    public Float getStep() {
        if (!tilesExist())
            return 0f;
        return elevationDataObject.getmElevationStep();
    }

    public void reset() {
        if (vertexList != null)
            vertexList.clear();
        if (tileList != null)
            tileList.clear();
        if (temp != null)
            temp.clear();
        if (texturesToUse != null)
            texturesToUse.clear();
        if (drawTiles != null)
            drawTiles.clear();
        if (vectorArrayList != null)
            vectorArrayList.clear();
        elevationDataObject = null;
        elevationDataLoaded = false;
        if (vectorArrayList != null)
            vectorArrayList.clear();
        if (vectorArrayList2d != null)
            vectorArrayList2d.clear();
        Layer.resetBasePoints();
        resetCurrentTiles();
        System.gc();

    }

    public ArrayList<ArrayList<Vector>> getVectorList() {
        return vectorArrayList;
    }

    public ArrayList<Vector> getVectorList1d() {
        return vectorArrayList2d;
    }

    public void prepare() {
        reset();
        TileStaticData.getInstance().destroy();
        clearDrawTiles();
        TextureCache.invalidate();
    }

    public int getVertexIndex(int y, int x) {
        return getElevationArray().get(0).size() * y + x;
    }


    public int[] getLeftBottomTapPosition(float x, float y, float xWithOffset, float yWithOffet, Course3DRenderer renderer) {
        //REMOVED DETECTING OF 2D VIEW BECAUSE OF INCORRECT Z_POSITION RENDERING FOR TREES AFTER 2D MODE IS TURNED OFF
        if (!tilesExist()) {
            return new int[4];
        }


        int bottomLeftIndexX = (int) ((Layer.transformToLon(x) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        int bottomLeftIndexY = (int) Math.ceil((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(y)) / elevationDataObject.getmElevationStep());

        int bottomLeftIndexXWithOffset = (int) ((Layer.transformToLon(xWithOffset) - elevationDataObject.getmElevationMinLongitude()) / elevationDataObject.getmElevationStep());
        int bottomLeftIndexYWithOffset = (int) Math.ceil((elevationDataObject.getmElevationMaxLatitude() - Layer.transformToLat(yWithOffet)) / elevationDataObject.getmElevationStep());

        int arrayHeight = vectorArrayList.size();
        int arrayWidth = vectorArrayList.get(0).size();

        if (bottomLeftIndexX > arrayWidth - 1)
            bottomLeftIndexX = arrayWidth - 1;
        else if (bottomLeftIndexX < 0)
            bottomLeftIndexX = 0;

        if (bottomLeftIndexXWithOffset > arrayWidth - 1)
            bottomLeftIndexXWithOffset = arrayWidth - 1;
        else if (bottomLeftIndexXWithOffset < 0)
            bottomLeftIndexXWithOffset = 0;

        if (bottomLeftIndexY > arrayHeight - 1)
            bottomLeftIndexY = arrayHeight - 1;
        else if (bottomLeftIndexY < 0)
            bottomLeftIndexY = 0;

        if (bottomLeftIndexYWithOffset > arrayHeight - 1)
            bottomLeftIndexYWithOffset = arrayHeight - 1;
        else if (bottomLeftIndexYWithOffset < 0)
            bottomLeftIndexYWithOffset = 0;

        int offset = 30;
        int extremeLeft = Math.min(bottomLeftIndexX, bottomLeftIndexXWithOffset);
        extremeLeft = extremeLeft - offset < 0 ? 0 : extremeLeft - offset;
        int extremeRight = Math.max(bottomLeftIndexX, bottomLeftIndexXWithOffset);
        extremeRight = Math.min(arrayWidth - 1, extremeRight + offset);
        int extremeTop = Math.min(bottomLeftIndexY, bottomLeftIndexYWithOffset);
        extremeTop = extremeTop - offset < 0 ? 0 : extremeTop - offset;
        int extremeBottom = Math.max(bottomLeftIndexY, bottomLeftIndexYWithOffset);
        extremeBottom = Math.min(arrayHeight - 1, extremeBottom + offset);

        return new int[]{extremeTop, extremeBottom, extremeLeft, extremeRight};
    }

}
