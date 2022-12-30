package com.l1inc.viewer.drawing;

import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.VectorMath;

import java.util.ArrayList;

/**
 * Created by Kirill Kartukov on 05.04.2018.
 */

public class BunkerTriangulator {

    ArrayList<ArrayList<Vector>> bunkerMapArray;
    ArrayList<Vector> vertexArray = new ArrayList<>();

    short[] indexList;
    float[] vertexList;
    float[] uvList;
    float[] normalList;
//    ArrayList<Float> normalListBottom = new ArrayList<>();
//    ArrayList<Integer> indexListBottom = new ArrayList<>();

    public BunkerTriangulator(ArrayList<ArrayList<Vector>> bunkerMapArray) {
        this.bunkerMapArray = bunkerMapArray;
    }


    public void triangulate() {
        try {
            for (int mapIndex = 0; mapIndex < bunkerMapArray.size(); mapIndex++) {
                if (mapIndex != bunkerMapArray.size() - 1) {
                    ArrayList<Vector> outsideMap = bunkerMapArray.get(mapIndex);
                    ArrayList<Vector> insideMap = bunkerMapArray.get(mapIndex + 1);

                    for (int i = 0; i < outsideMap.size(); i++) {
                        vertexArray.add(outsideMap.get(i));
//                        if (mapIndex + 1 == bunkerMapArray.size() - 1) {
//                            indexListBottom.add(vertexArray.size());
//                        }
                        vertexArray.add(insideMap.get(i));

                    }
                }
            }

            int mapPointCount = bunkerMapArray.get(0).size();

            indexList = new short[vertexArray.size() * 3];
            int counter = 0;

            for (int mapIndex = 0; mapIndex < bunkerMapArray.size(); mapIndex++) {

                if (mapIndex != bunkerMapArray.size() - 1) {

                    for (int i = 0; i < mapPointCount; i++) {

                        Vector v1, v2, v3, normal;

                        int firstIndex = mapIndex * mapPointCount * 2;
                        int t = i * 2 + firstIndex;

                        if (i != mapPointCount - 1) {

                            indexList[counter++] = (short) (t + 3);
                            indexList[counter++] = (short) (t + 1);
                            indexList[counter++] = (short) t;

                            v1 = vertexArray.get(t + 3);
                            v2 = vertexArray.get(t + 1);
                            v3 = vertexArray.get(t);

                            normal = VectorMath.getNormals2(v1, v2, v3);

                            v1.setNormals(normal);
                            v2.setNormals(normal);
                            v3.setNormals(normal);


                            indexList[counter++] = (short) (t + 2);
                            indexList[counter++] = (short) (t + 3);
                            indexList[counter++] = (short) t;

                            v1 = vertexArray.get(t + 2);
                            v2 = vertexArray.get(t + 3);
                            v3 = vertexArray.get(t);

                            normal = VectorMath.getNormals2(v1, v2, v3);

                            v1.setNormals(normal);
                            v2.setNormals(normal);
                            v3.setNormals(normal);

                        } else {

                            indexList[counter++] = (short) (firstIndex + 1);
                            indexList[counter++] = (short) (t + 1);
                            indexList[counter++] = (short) t;

                            v1 = vertexArray.get(firstIndex + 1);
                            v2 = vertexArray.get(t + 1);
                            v3 = vertexArray.get(t);

                            normal = VectorMath.getNormals2(v1, v2, v3);

                            v1.setNormals(normal);
                            v2.setNormals(normal);
                            v3.setNormals(normal);


                            indexList[counter++] = (short) firstIndex;
                            indexList[counter++] = (short) (firstIndex + 1);
                            indexList[counter++] = (short) t;

                            v1 = vertexArray.get(firstIndex);
                            v2 = vertexArray.get(firstIndex + 1);
                            v3 = vertexArray.get(t);

                            normal = VectorMath.getNormals2(v1, v2, v3);

                            v1.setNormals(normal);
                            v2.setNormals(normal);
                            v3.setNormals(normal);

                        }

                    }

                }

            }

            vertexList = new float[vertexArray.size() * 3];
            uvList = new float[vertexArray.size() * 2];
            normalList = new float[vertexArray.size() * 3];

            for (int i = 0; i < vertexArray.size(); i++) {
                Vector vector = vertexArray.get(i);

                vertexList[i * 3] = (float) vector.x;
                vertexList[i * 3 + 1] = (float) vector.y;
                vertexList[i * 3 + 2] = (float) vector.z;

                uvList[i * 2] = (float) vector.x;
                uvList[i * 2 + 1] = (float) vector.y;

                normalList[i * 3] = (float) vector.normalX;
                normalList[i * 3 + 1] = (float) vector.normalY;
                normalList[i * 3 + 2] = (float) vector.normalZ;

//                if (indexListBottom.contains(i)) {
//                    normalListBottom.add((float) vector.normalX);
//                    normalListBottom.add((float) vector.normalY);
//                    normalListBottom.add((float) vector.normalZ);
//                }
            }

//            normalListBottom.add(normalListBottom.get(0));
//            normalListBottom.add(normalListBottom.get(1));
//            normalListBottom.add(normalListBottom.get(2));

            /*Log.e(getClass().getSimpleName(), vertexArray.size()+"   aaaa g");
            Log.e(getClass().getSimpleName(), testUV.size()+"   aaaa");
            uvList = new float[testUV.size()];
            for(int i = 0; i< testUV.size();i++){
                uvList[i] = (float) testUV.get(i);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
