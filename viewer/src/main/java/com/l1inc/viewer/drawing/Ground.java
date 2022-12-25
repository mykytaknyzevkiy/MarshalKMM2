package com.l1inc.viewer.drawing;

import android.content.Context;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.Course3DRendererBase;
import com.l1inc.viewer.R;
import com.l1inc.viewer.elevation.ElevationHelper;
import com.l1inc.viewer.elevation.QuadNode;
import com.l1inc.viewer.elevation.TileStaticData;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;
import com.l1inc.viewer.elevation.GroundNode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 4/21/2016.
 */
public class Ground extends BaseDrawingObject {

    private int[] vertexBuffers = new int[1];
    private int[] normalsBuffers = new int[1];
    private int[] textureBuffers = new int[1];
    private int[] indexBuffers = new int[1];

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer normalBuffer;
    private ShortBuffer indexBuffer;
    private int vertexCount;

    private short[] indices;
    public int textureId= -1;
    private int texture2DId = -1;
    public List<Float> vertexList = new ArrayList<>();

    public int dotCount;

    float tileZheight = 0;

    private ArrayList<QuadNode> quadNodes2x2 = new ArrayList<>();
    private ArrayList<QuadNode> quadNodes4x4 = new ArrayList<>();

    private boolean nodeGeneratingSuccess = true;

    private int textureResId = -1;
    private int textureResId2 = -1;
    private Context context;

    private void loadTexture(){
        textureId = !ETC1Util.isETC1Supported() ? TextureCache.getTexture(context, textureResId) : TextureCache.getCompressedTexture(context, textureResId);
        texture2DId = !ETC1Util.isETC1Supported() ? TextureCache.getTexture(context, textureResId2) : TextureCache.getCompressedTexture(context, textureResId2);

    }

    public Ground(Course3DRenderer renderer, int resId, int resId2D, Context context) {
        this.context = context;
        this.textureResId = resId;
        this.textureResId2 = resId2D;

        List<Float> uvList = new ArrayList<>();
        float xC;
        float yC;
        float zC;

        if (renderer.elevationDataExist && ElevationHelper.getInstance().tilesExist() && TileStaticData.getInstance().dataExist()) {
            vertexList.clear();
            for (int y = 0; y < ElevationHelper.getInstance().getElevationArray().size(); y++) {
                for (int x = 0; x < ElevationHelper.getInstance().getElevationArray().get(y).size(); x++) {

                    yC = (float) Layer.transformLat((ElevationHelper.getInstance().getMaxLatitude() - ElevationHelper.getInstance().getStep() * (float) y));
                    xC = (float) Layer.transformLon((ElevationHelper.getInstance().getMinLongitude() + ElevationHelper.getInstance().getStep() * (float) x));
                    zC = (ElevationHelper.getInstance().getElevationArray().get(y).get(x) - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE;

                    vertexList.add(xC);
                    vertexList.add(yC);
                    vertexList.add(zC);

                    uvList.add(xC);
                    uvList.add(yC);
                }
            }
        }

        loadTexture();

        vertexCount = vertexList.size();


        if (renderer.elevationDataExist&& ElevationHelper.getInstance().tilesExist() && TileStaticData.getInstance().dataExist()) {
            int mapHeight = ElevationHelper.getInstance().getElevationArray().size();
            int mapWidth = ElevationHelper.getInstance().getElevationArray().get(0).size();

            int maxIndex = mapWidth - 2 + (mapHeight - 2) * mapWidth;
            indices = new short[3 * 2 * (mapWidth - 1) * (mapHeight - 1)];
            int vertIndex = 0;

            for (int i = 0; i <= mapHeight - 2; i++) {
                for (int j = 0; j <= mapWidth - 2; j++) {
                    int t = j + i * mapWidth;
                    indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                    indices[vertIndex++] = (short) (t + 1); // Upper Right
                    indices[vertIndex++] = (short) t; // Upper Left

                    indices[vertIndex++] = (short) (t + mapWidth); // Bottom Left
                    indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                    indices[vertIndex++] = (short) t; // Upper left
                }
            }

            indexBuffer = ShortBuffer.allocate(indices.length * 2/*потому что short это два байта*/).put(indices);
            indexBuffer.position(0);


        }

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        for (Float val : vertexList) {
            vertexBuffer.put(val);
        }


        ByteBuffer byteBuf = ByteBuffer.allocateDirect(uvList.size() * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        for (Float val : uvList) {
            textureBuffer.put(val);
        }
        textureBuffer.position(0);

        vbb = null;
        byteBuf = null;
        uvList = null;
        vertexList = null;


    }

    private void addLog(String mes) {
        Log.e(getClass().getSimpleName(), mes);
    }

    private GroundNode[] nodeArray = new GroundNode[64];

    public Ground(Course3DRenderer renderer, int resId, int resId2D, Context context, int startRow, int height, int rowsByOneGroundTile, int i2, int rowsLeft, boolean isLast, int vertexStartCount) {
        this.context = context;
        this.textureResId = resId;
        this.textureResId2 = resId2D;
        rowsByOneGroundTile++;

        if (isLast && rowsLeft > rowsByOneGroundTile)
            rowsByOneGroundTile = rowsLeft;

        List<Float> vertexList = new ArrayList<>();
        List<Vector> normalsList = new ArrayList<>();
        List<Float> uvList = new ArrayList<>();
        float xC;
        float yC;
        float zC;


        int nodeYCount = 8;
        int nodeXCount = 8;

        int stepX = ElevationHelper.getInstance().getElevationArray().get(0).size() / nodeXCount;
        int stepY = ((startRow + rowsByOneGroundTile > height ? height : startRow + rowsByOneGroundTile) - startRow) / nodeYCount;

        int rowWidth = ElevationHelper.getInstance().getElevationArray().get(0).size();

        if (renderer.elevationDataExist && ElevationHelper.getInstance().tilesExist() && TileStaticData.getInstance().dataExist()) {

            vertexList.clear();
            normalsList.clear();

            int startRowX = 0;

            int maxHeight = (startRow + rowsByOneGroundTile > height ? height : startRow + rowsByOneGroundTile);

            float centerZ = ((ElevationHelper.getInstance().mMaxHeight - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE) / 2;
            tileZheight = Math.abs((ElevationHelper.getInstance().mMaxHeight - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE);

            loadTexture();

            try {

                nodeArray[0] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[1] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[2] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[3] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[4] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[5] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[6] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[7] = new GroundNode(renderer, resId, context, startRow, startRow + stepY, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                nodeArray[8] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[9] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[10] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[11] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[12] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[13] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[14] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[15] = new GroundNode(renderer, resId, context, (startRow + stepY - 1), startRow + stepY * 2, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                nodeArray[16] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[17] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[18] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[19] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[20] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[21] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[22] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[23] = new GroundNode(renderer, resId, context, (startRow + stepY * 2 - 1), startRow + stepY * 3, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                nodeArray[24] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[25] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[26] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[27] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[28] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[29] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[30] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[31] = new GroundNode(renderer, resId, context, (startRow + stepY * 3 - 1), startRow + stepY * 4, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                nodeArray[32] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[33] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[34] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[35] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[36] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[37] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[38] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[39] = new GroundNode(renderer, resId, context, (startRow + stepY * 4 - 1), startRow + stepY * 5, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                nodeArray[40] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[41] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[42] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[43] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[44] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[45] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[46] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[47] = new GroundNode(renderer, resId, context, (startRow + stepY * 5 - 1), startRow + stepY * 6, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                nodeArray[48] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[49] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[50] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[51] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[52] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[53] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[54] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[55] = new GroundNode(renderer, resId, context, (startRow + stepY * 6 - 1), startRow + stepY * 7, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                nodeArray[56] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, 0, startRowX + stepX, centerZ, tileZheight);
                nodeArray[57] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, (startRowX + stepX - 1), startRowX + stepX * 2, centerZ, tileZheight);
                nodeArray[58] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, (startRowX + stepX * 2 - 1), startRowX + stepX * 3, centerZ, tileZheight);
                nodeArray[59] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, (startRowX + stepX * 3 - 1), startRowX + stepX * 4, centerZ, tileZheight);
                nodeArray[60] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, (startRowX + stepX * 4 - 1), startRowX + stepX * 5, centerZ, tileZheight);
                nodeArray[61] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, (startRowX + stepX * 5 - 1), startRowX + stepX * 6, centerZ, tileZheight);
                nodeArray[62] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, (startRowX + stepX * 6 - 1), startRowX + stepX * 7, centerZ, tileZheight);
                nodeArray[63] = new GroundNode(renderer, resId, context, (startRow + stepY * 7 - 1), maxHeight, (startRowX + stepX * 7 - 1), rowWidth, centerZ, tileZheight);

                quadNodes2x2 = new ArrayList<>();
                quadNodes4x4 = new ArrayList<>();

                //0,1,8,9 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[0].getStartExtremePoints(), nodeArray[9].getEndExtremePoints()), centerZ, nodeArray[0], nodeArray[1], nodeArray[8], nodeArray[9]));
                //2,3,10,11
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[2].getStartExtremePoints(), nodeArray[11].getEndExtremePoints()), centerZ, nodeArray[2], nodeArray[3], nodeArray[10], nodeArray[11]));
                //16,17,24,25
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[16].getStartExtremePoints(), nodeArray[25].getEndExtremePoints()), centerZ, nodeArray[16], nodeArray[17], nodeArray[24], nodeArray[25]));
                //18,19,26,27
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[18].getStartExtremePoints(), nodeArray[27].getEndExtremePoints()), centerZ, nodeArray[18], nodeArray[19], nodeArray[26], nodeArray[27]));

                //todo add to 4 x 4
                quadNodes4x4.add(new QuadNode(getNodePositionData(nodeArray[0].getStartExtremePoints(), nodeArray[27].getEndExtremePoints()), centerZ, new ArrayList<QuadNode>(quadNodes2x2)));
                quadNodes2x2.clear();
                quadNodes2x2 = new ArrayList<>();

                //4,5,12,13 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[4].getStartExtremePoints(), nodeArray[13].getEndExtremePoints()), centerZ, nodeArray[4], nodeArray[5], nodeArray[12], nodeArray[13]));
                //6,7,14,15 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[6].getStartExtremePoints(), nodeArray[15].getEndExtremePoints()), centerZ, nodeArray[6], nodeArray[7], nodeArray[14], nodeArray[15]));
                //20,21,28,29 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[20].getStartExtremePoints(), nodeArray[29].getEndExtremePoints()), centerZ, nodeArray[20], nodeArray[21], nodeArray[28], nodeArray[29]));
                //22,23,30,31 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[22].getStartExtremePoints(), nodeArray[31].getEndExtremePoints()), centerZ, nodeArray[22], nodeArray[23], nodeArray[30], nodeArray[31]));

                //todo add to 4 x 4
                quadNodes4x4.add(new QuadNode(getNodePositionData(nodeArray[4].getStartExtremePoints(), nodeArray[31].getEndExtremePoints()), centerZ, new ArrayList<QuadNode>(quadNodes2x2)));
                quadNodes2x2.clear();
                quadNodes2x2 = new ArrayList<>();

                //32,33,40,41 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[32].getStartExtremePoints(), nodeArray[41].getEndExtremePoints()), centerZ, nodeArray[32], nodeArray[33], nodeArray[40], nodeArray[41]));
                //34,35,42,43 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[34].getStartExtremePoints(), nodeArray[43].getEndExtremePoints()), centerZ, nodeArray[34], nodeArray[35], nodeArray[42], nodeArray[43]));
                //48,49,56,57 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[48].getStartExtremePoints(), nodeArray[57].getEndExtremePoints()), centerZ, nodeArray[48], nodeArray[49], nodeArray[56], nodeArray[57]));
                //50,51,58,59 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[50].getStartExtremePoints(), nodeArray[59].getEndExtremePoints()), centerZ, nodeArray[50], nodeArray[51], nodeArray[58], nodeArray[59]));

                //todo add to 4 x 4
                quadNodes4x4.add(new QuadNode(getNodePositionData(nodeArray[32].getStartExtremePoints(), nodeArray[59].getEndExtremePoints()), centerZ, new ArrayList<QuadNode>(quadNodes2x2)));
                quadNodes2x2.clear();
                quadNodes2x2 = new ArrayList<>();

                //36,37,44,45 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[36].getStartExtremePoints(), nodeArray[45].getEndExtremePoints()), centerZ, nodeArray[36], nodeArray[37], nodeArray[44], nodeArray[45]));
                //38,39,46,47 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[38].getStartExtremePoints(), nodeArray[47].getEndExtremePoints()), centerZ, nodeArray[38], nodeArray[39], nodeArray[46], nodeArray[47]));
                //52,53,60,61 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[52].getStartExtremePoints(), nodeArray[61].getEndExtremePoints()), centerZ, nodeArray[52], nodeArray[53], nodeArray[60], nodeArray[61]));
                //54,55,62,63 positions
                quadNodes2x2.add(new QuadNode(getNodePositionData(nodeArray[54].getStartExtremePoints(), nodeArray[63].getEndExtremePoints()), centerZ, nodeArray[54], nodeArray[55], nodeArray[62], nodeArray[63]));

                //todo add to 4 x 4
                quadNodes4x4.add(new QuadNode(getNodePositionData(nodeArray[36].getStartExtremePoints(), nodeArray[63].getEndExtremePoints()), centerZ, new ArrayList<QuadNode>(quadNodes2x2)));
                quadNodes2x2.clear();

                nodeArray = null;

                return;

            } catch (Exception e) {
                e.printStackTrace();
                nodeArray = new GroundNode[0];
                quadNodes2x2.clear();
                quadNodes4x4.clear();
                nodeGeneratingSuccess = false;

                if (ElevationHelper.getInstance().getElevationArray() != null) {
                    for (int y = startRow; y < (startRow + rowsByOneGroundTile > height ? height : startRow + rowsByOneGroundTile); y++) {
                        for (int x = 0; x < ElevationHelper.getInstance().getElevationArray().get(y).size(); x++) {
                            yC = (float) Layer.transformLat((ElevationHelper.getInstance().getMaxLatitude() - ElevationHelper.getInstance().getStep() * (float) y));
                            xC = (float) Layer.transformLon((ElevationHelper.getInstance().getMinLongitude() + ElevationHelper.getInstance().getStep() * (float) x));
                            zC = (ElevationHelper.getInstance().getElevationArray().get(y).get(x) - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE;
                            vertexList.add(xC);
                            vertexList.add(yC);
                            vertexList.add(zC);
                            uvList.add(xC);
                            uvList.add(yC);
                        }
                    }
                }
            } catch (OutOfMemoryError e){
                e.printStackTrace();
                nodeArray = new GroundNode[0];
                quadNodes2x2.clear();
                quadNodes4x4.clear();
                nodeGeneratingSuccess = false;

                if (ElevationHelper.getInstance().getElevationArray() != null) {
                    for (int y = startRow; y < (startRow + rowsByOneGroundTile > height ? height : startRow + rowsByOneGroundTile); y++) {
                        for (int x = 0; x < ElevationHelper.getInstance().getElevationArray().get(y).size(); x++) {
                            yC = (float) Layer.transformLat((ElevationHelper.getInstance().getMaxLatitude() - ElevationHelper.getInstance().getStep() * (float) y));
                            xC = (float) Layer.transformLon((ElevationHelper.getInstance().getMinLongitude() + ElevationHelper.getInstance().getStep() * (float) x));
                            zC = (ElevationHelper.getInstance().getElevationArray().get(y).get(x) - ElevationHelper.getInstance().mMinHeight) / Constants.LOCATION_SCALE;
                            vertexList.add(xC);
                            vertexList.add(yC);
                            vertexList.add(zC);
                            uvList.add(xC);
                            uvList.add(yC);
                        }
                    }
                }
            }


            vertexCount = vertexList.size();

            indices = new short[0];

            if (renderer.elevationDataExist&& ElevationHelper.getInstance().tilesExist() && TileStaticData.getInstance().dataExist()) {
                int mapHeight = rowsLeft < rowsByOneGroundTile ? rowsLeft : rowsByOneGroundTile;
                int mapWidth = ElevationHelper.getInstance().getElevationArray().get(0).size();

                indices = new short[3 * 2 * (mapWidth - 1) * (mapHeight - 1)];
                int vertIndex = 0;


                for (int i = 0; i <= mapHeight - 2; i++) {
                    for (int j = 0; j <= mapWidth - 2; j++) {
                        int t = j + i * mapWidth;

                        indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                        indices[vertIndex++] = (short) (t + 1); // Upper Right
                        indices[vertIndex++] = (short) t; // Upper Left

                        indices[vertIndex++] = (short) (t + mapWidth); // Bottom Left
                        indices[vertIndex++] = (short) (t + mapWidth + 1); // Bottom Right
                        indices[vertIndex++] = (short) t; // Upper left

                    }
                }

                indexBuffer = ShortBuffer.allocate(indices.length * 2/*потому что short это два байта*/).put(indices);
                indexBuffer.position(0);

            }


            normalsList = new ArrayList<>(ElevationHelper.getInstance().vectorArrayList1D.subList(vertexStartCount, vertexStartCount + vertexCount / 3));

            int vs, ts, ns, is;
            is = indices.length;
            vs = vertexCount;
            ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 4);
            vbb.order(ByteOrder.nativeOrder());
            vertexBuffer = vbb.asFloatBuffer();
            for (Float val : vertexList) {
                vertexBuffer.put(val);
            }

            vertexBuffer.position(0);

            vbb.clear();
            vertexList.clear();

            ts = uvList.size();
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(uvList.size() * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            textureBuffer = byteBuf.asFloatBuffer();
            for (Float val : uvList) {
                textureBuffer.put(val);
            }
            textureBuffer.position(0);

            byteBuf.clear();
            uvList.clear();

            ns = vertexCount;
            ByteBuffer normalsByteBuffer = ByteBuffer.allocateDirect(vertexCount * 4);
            normalsByteBuffer.order(ByteOrder.nativeOrder());
            normalBuffer = normalsByteBuffer.asFloatBuffer();
            for (Vector val : normalsList) {
                normalBuffer.put((float) val.normalX);
                normalBuffer.put((float) val.normalY);
                normalBuffer.put((float) val.normalZ);
            }

            normalBuffer.position(0);


            normalsByteBuffer.clear();
            normalsList.clear();

            normalBuffer.capacity();

            GLES20.glGenBuffers(1, vertexBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vs * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glGenBuffers(1, normalsBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, ns * 4, normalBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glGenBuffers(1, textureBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, ts * 4, textureBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glGenBuffers(1, indexBuffers, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffers[0]);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, is * 2, indexBuffer, GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

            dotCount = vertexStartCount + vertexCount / 3 - ElevationHelper.getInstance().getMapWidth() * 2;
        }
    }

    public void drawSimpleGround(Course3DRenderer renderer) {

        if(texture2DId == -1 || textureId == -1 || !TextureCache.hasTexture(textureResId2, texture2DId) || !TextureCache.hasTexture(textureResId,textureId)) {
            loadTexture();
        }

        if (renderer.vertexBuffer2DGround == null || renderer.textureBuffer2DGround == null)
            return;
        renderer.vertexBuffer2DGround.position(0);
        renderer.textureBuffer2DGround.position(0);
        drawObjectWithArrays(renderer, GLES20.GL_TRIANGLE_FAN, renderer.getNavigationMode() == Course3DRendererBase.NavigationMode.NavigationMode2D ? texture2DId : textureId, renderer.vertexBuffer2DGround, renderer.textureBuffer2DGround, renderer.getVertexCount2D() / 3, 1);
    }

    public void bindTexture(Course3DRenderer renderer){
        if(texture2DId == -1 || textureId == -1 || !TextureCache.hasTexture(textureResId2,texture2DId) || !TextureCache.hasTexture(textureResId,textureId)) {
            loadTexture();
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderer.getNavigationMode() == Course3DRendererBase.NavigationMode.NavigationMode2D ? texture2DId : textureId);
    }

    public int getTextureId() {
        return textureId;
    }

    public void draw(final Course3DRenderer renderer,
                     final int pos,
                     final float x,
                     final float y,
                     final float z) {
        if (!renderer.elevationDataExist || renderer.getNavigationMode() == Course3DRendererBase.NavigationMode.NavigationMode2D) {
            drawObjectWithArrays(renderer, GLES20.GL_TRIANGLE_FAN, renderer.getNavigationMode() == Course3DRendererBase.NavigationMode.NavigationMode2D ? texture2DId : textureId, renderer.vertexBuffer2DGround, renderer.textureBuffer2DGround, renderer.getVertexCount2D() / 3, 1);
        } else {
            if (nodeGeneratingSuccess) {
                for (QuadNode qd : quadNodes4x4)
                    qd.intersect(renderer, pos, x, y, z);
            } else {
                drawWithBuffers(renderer);
            }
        }
    }

    private void drawWithBuffers(Course3DRenderer renderer) {
        GLES20.glUniformMatrix4fv(renderer.modelViewMatrixLocation, 1, false, renderer.modelViewMatrix, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalsBuffers[0]);
        GLES20.glEnableVertexAttribArray(renderer.normalLocation);
        GLES20.glVertexAttribPointer(renderer.normalLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glEnableVertexAttribArray(renderer.shadowPositionLocation);
        GLES20.glVertexAttribPointer(renderer.shadowPositionLocation, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureBuffers[0]);
        GLES20.glEnableVertexAttribArray(renderer.shadowTexCoord);
        GLES20.glVertexAttribPointer(renderer.shadowTexCoord, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffers[0]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private float[] getNodePositionData(int[] startPositions, int[] endPositions) {

        Vector position = VectorMath.multiplied(VectorMath.added(
                ElevationHelper.getInstance().getVectorList().get(endPositions[0] - 1).get(endPositions[1] - 1),
                ElevationHelper.getInstance().getVectorList().get(startPositions[0]).get(startPositions[1])), 0.5);

        float nodeHeight, nodeWidth;
        nodeHeight = Math.abs((float) ElevationHelper.getInstance().getVectorList().get(endPositions[0] - 1).get(startPositions[1]).y - (float) ElevationHelper.getInstance().getVectorList().get(startPositions[0]).get(startPositions[1]).y);
        nodeWidth = Math.abs((float) ElevationHelper.getInstance().getVectorList().get(startPositions[0]).get(endPositions[1] - 1).x - (float) ElevationHelper.getInstance().getVectorList().get(startPositions[0]).get(startPositions[1]).x);


        return new float[]{(float) position.x, (float) position.y, nodeWidth / 2, nodeHeight / 2, tileZheight / 2};
    }

    public void destroy() {
        for (QuadNode qd : quadNodes4x4) {
            qd.destroy();
        }

        if (vertexBuffers[0] != 0) {
            GLES20.glDeleteBuffers(1, vertexBuffers, 0);
            vertexBuffers[0] = 0;
        }

        if (normalsBuffers[0] != 0) {
            GLES20.glDeleteBuffers(1, normalsBuffers, 0);
            normalsBuffers[0] = 0;
        }

        if (textureBuffers[0] != 0) {
            GLES20.glDeleteBuffers(1, textureBuffers, 0);
            textureBuffers[0] = 0;
        }

        if (indexBuffers[0] != 0) {
            GLES20.glDeleteBuffers(1, indexBuffers, 0);
            indexBuffers[0] = 0;
        }
    }


}
