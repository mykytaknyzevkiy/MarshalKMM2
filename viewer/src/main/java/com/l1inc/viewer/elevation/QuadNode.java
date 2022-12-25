package com.l1inc.viewer.elevation;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.Course3DViewer;
import com.l1inc.viewer.math.Frustum;
import com.l1inc.viewer.math.Vector;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Kirill Kartukov on 18.01.2018.
 */

public class QuadNode {

    private float centerX;
    private float centerY;
    private float centerZ;
    private float radius;
    private float zHlfSize;
    private float yHlfSize;
    private float xHlfSize;
    private float nodeHeight;
    private float nodeWidth;

    private boolean isRoot = false;

    private ArrayList<GroundNode> groundNodes = new ArrayList<>();

    private ArrayList<QuadNode> quadNodesArray = new ArrayList<>();

    public QuadNode(float[] positionData, float centerZ, ArrayList<QuadNode> nodesArray) {
        this.centerX = positionData[0];
        this.centerY = positionData[1];
        this.centerZ = centerZ;
        this.zHlfSize = positionData[4];
        this.yHlfSize = positionData[3];
        this.xHlfSize = positionData[2];
        this.quadNodesArray = nodesArray;
        isRoot = true;

        addLog("!!!ROOT NODE : " + toString());
    }

    public QuadNode(float[] positionData, float centerZ, GroundNode... groundNode) {
        this.centerX = positionData[0];
        this.centerY = positionData[1];
        this.centerZ = centerZ;
        this.zHlfSize = positionData[4];
        this.yHlfSize = positionData[3];
        this.xHlfSize = positionData[2];


        addLog("NOT ROOT NODE : " + toString());

        if (groundNode.length > 0)
            groundNodes.addAll(Arrays.asList(groundNode));
    }

    private void addLog(String mes) {
        // Log.e(getClass().getSimpleName(), mes);
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public float getCenterZ() {
        return centerZ;
    }

    public void setCenterZ(float centerZ) {
        this.centerZ = centerZ;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getzHlfSize() {
        return zHlfSize;
    }

    public void setzHlfSize(float zHlfSize) {
        this.zHlfSize = zHlfSize;
    }

    public float getyHlfSize() {
        return yHlfSize;
    }

    public void setyHlfSize(float yHlfSize) {
        this.yHlfSize = yHlfSize;
    }

    public float getxHlfSize() {
        return xHlfSize;
    }

    public void setxHlfSize(float xHlfSize) {
        this.xHlfSize = xHlfSize;
    }

    public float getNodeHeight() {
        return nodeHeight;
    }

    public void setNodeHeight(float nodeHeight) {
        this.nodeHeight = nodeHeight;
    }

    public float getNodeWidth() {
        return nodeWidth;
    }

    public void setNodeWidth(float nodeWidth) {
        this.nodeWidth = nodeWidth;
    }

    public ArrayList<QuadNode> getQuadNodesArray() {
        return quadNodesArray;
    }

    public void setQuadNodesArray(ArrayList<QuadNode> quadNodesArray) {
        this.quadNodesArray = quadNodesArray;
    }

    public void intersect(final Course3DRenderer renderer,
                          final int pos,
                          final float x,
                          final float y,
                          final float z) {
        if (Frustum.cubeInFrustum(centerX, centerY, centerZ, xHlfSize, yHlfSize, zHlfSize)) {
            if (!isRoot) {
                final Vector cameraPos = new Vector(-x, -y);
                for (GroundNode gr : groundNodes) {
                    if (Course3DViewer.isLightweightRendering()) {
                        final Vector grCenterPos = new Vector(
                                gr.getCenterX(), gr.getCenterY());
                        final double distance = grCenterPos.distance(cameraPos);
                        if (distance < 40) {
                            gr.draw(renderer, pos);
                        }
                    } else {
                        gr.draw(renderer, pos);
                    }
                }
            } else {
                for (QuadNode qN : quadNodesArray) {
                    // addLog("root rect, going deeper");
                    qN.intersect(renderer, pos, x, y, z);
                }
            }
        } else {
            // addLog("cube is not in frustrum");
        }
    }

    public void destroy() {
        if (!isRoot) {
            for (GroundNode node : groundNodes) {
                node.destroy();
            }
        } else {
            for (QuadNode qN : quadNodesArray) {
                qN.destroy();
            }
        }
    }

    @Override
    public String toString() {
        return "QuadNode{" +
                "centerX=" + centerX +
                ", centerY=" + centerY +
                ", centerZ=" + centerZ +
                ", radius=" + radius +
                ", zHlfSize=" + zHlfSize +
                ", yHlfSize=" + yHlfSize +
                ", xHlfSize=" + xHlfSize +
                ", nodeHeight=" + nodeHeight +
                ", nodeWidth=" + nodeWidth +
                ", isRoot=" + isRoot +
                '}';
    }
}
