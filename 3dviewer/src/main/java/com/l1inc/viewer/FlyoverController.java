package com.l1inc.viewer;

import android.util.Log;

import com.l1inc.viewer.drawing.Layer;
import com.l1inc.viewer.drawing.PointListLayer;
import com.l1inc.viewer.math.Interpolator;
import com.l1inc.viewer.math.LinearInterpolator;
import com.l1inc.viewer.math.PathWalker;
import com.l1inc.viewer.math.RotationHelper;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.math.WaitFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 4/13/2016.
 */
public class FlyoverController {

    enum AnimationState {
        Wait,
        FlyToGreencenter,
        TiltGreencenter,
        WaitForFinish,
        //ZoomToGreencenter
    }


    private static final int ROTATION_SPEED = 2;

    private PointListLayer centralpath;
    private PathWalker pathWalker;
    private RotationHelper rotationHelper;
    private LinearInterpolator viewInterpolator;
    private LinearInterpolator zoomInterpolator;
    private AnimationState animationState = AnimationState.Wait;
    private Vector[] centralpathFowrard;
    private double defaultViewAngle;
    private WaitFunction waitFunction;
    private Boolean finished;
    private double defaultZoom;
    private ElapsedTimeCalculator elapsedTimeCalculator = new ElapsedTimeCalculator();
    private boolean flyingFinished = false;

    private double flyingSpeed = 1.7;

    public void setCentralpath(PointListLayer centralpath, Layer green) {

        lastTickVector = new Vector(0, 0);
        this.centralpath = centralpath;

        Vector[] pointList = new Vector[0];
        List<Vector> pointListArray = Arrays.asList(centralpath.getPointList().get(0).getPointList());
        if (green != null)
            pointListArray = deleteRedundantPointsInCentralPath(pointListArray, green);
        try {
            List<Vector> list = Interpolator.interpolate(pointListArray, 10, Interpolator.CatmullRomType.Centripetal);
            pointList = list.toArray(new Vector[list.size()]);
        } catch (Exception e) {
            Course3DRenderer.getViewerLogger().error(e);
        }

        centralpathFowrard = Arrays.copyOf(pointList, pointList.length);

    }


    public Vector[] getCentralpathFowrard() {
        return centralpathFowrard;
    }

    public void setFlyingSpeed(double flyingSpeed) {
        this.flyingSpeed = flyingSpeed;
    }

    public void setDefaultViewAngle(double defaultViewAngle) {
        this.defaultViewAngle = defaultViewAngle;
    }

    public Vector getPosition() {
        return pathWalker.getPosition();
    }

    public double getRotationAngle() {
        return rotationHelper.getCurrentValue();
    }

    public double getViewAngle() {
        if (animationState == AnimationState.TiltGreencenter || animationState == AnimationState.WaitForFinish) {
            return viewInterpolator.getCurrentValue();
        }

        return defaultViewAngle;
    }

    public void setDefaultZoom(double defaultZoom) {
        this.defaultZoom = defaultZoom;
    }

    public double getZoom() {
        if (animationState == AnimationState.TiltGreencenter || animationState == AnimationState.WaitForFinish) {
            return zoomInterpolator.getCurrentValue();
        }
        return defaultZoom;
    }

    public Boolean isFinished() {
        return finished;
    }

    public void start() {
        finished = false;
        flyToGreenCenter();
    }

    private Vector lastTickVector = new Vector();

    public boolean isFlyingFinished() {
        return flyingFinished;
    }

    public void tick() {
        lastTickVector = pathWalker.getPosition();
        elapsedTimeCalculator.tick();
        elapsedTimeCalculator.getTimeElapsed();
        if (animationState == AnimationState.FlyToGreencenter) {

            pathWalker.tick(elapsedTimeCalculator.getTimeElapsed());
            rotationHelper.setEndValue(pathWalker.getAngle());
            rotationHelper.tick(elapsedTimeCalculator.getTimeElapsed());
            if (pathWalker.isFinished()) {
                flyingFinished = true;
                tiltGreencenter();

            }
        }

        if (animationState == AnimationState.TiltGreencenter) {

            zoomInterpolator.tick(elapsedTimeCalculator.getTimeElapsed());
            if (zoomInterpolator.getCompletePercentage() > 0.75)
                viewInterpolator.tick(elapsedTimeCalculator.getTimeElapsed());
            if (viewInterpolator.isFinished() && zoomInterpolator.isFinished()) {
                waitForFinish();
            }
        }

        if (animationState == AnimationState.WaitForFinish) {
            waitFunction.tick(elapsedTimeCalculator.getTimeElapsed());

            if (waitFunction.isFinished()) {
                finished = true;
            }
        }

    }

    public void testTick() {
        if (animationState == AnimationState.FlyToGreencenter) {
            pathWalker.tick(60);
            rotationHelper.setEndValue(pathWalker.getAngle());
            rotationHelper.tick(60);

            if (pathWalker.isFinished()) {
                finished = true;
            }
        }


    }

    public Vector pause() {
        elapsedTimeCalculator.pause();
        return lastTickVector;
    }

    public void resume() {
        elapsedTimeCalculator.resume();
    }

    private void flyToGreenCenter() {
        animationState = AnimationState.FlyToGreencenter;

        pathWalker = new PathWalker();
        pathWalker.setPath(centralpathFowrard);
        pathWalker.setSpeed(flyingSpeed);
        pathWalker.start();

        double angle = pathWalker.getAngle();
        rotationHelper = new RotationHelper();
        rotationHelper.setEndValue(angle);
        rotationHelper.setSpeed(ROTATION_SPEED);
    }

    public double getCompletePercentage() {
        return pathWalker == null || pathWalker.getCompletePercentage() == 0 ? 0 : pathWalker.getCompletePercentage();
    }

    private void tiltGreencenter() {
        animationState = AnimationState.TiltGreencenter;

        viewInterpolator = new LinearInterpolator();
        viewInterpolator.setStartValue(defaultViewAngle);
        viewInterpolator.setEndValue(defaultViewAngle - 10);
        viewInterpolator.setSpeed(4.5);
        viewInterpolator.start();

        /*zoomSpeedIncrInterpolator = new LinearInterpolator();
        zoomSpeedIncrInterpolator.setStartValue(1);
        zoomSpeedIncrInterpolator.setEndValue(calculateStartZoomSpeed());
        zoomSpeedIncrInterpolator.setSpeed(15);
        zoomSpeedIncrInterpolator.start();

        zoomSpeedDecrInterpolator = new LinearInterpolator();
        zoomSpeedDecrInterpolator.setStartValue(calculateStartZoomSpeed());
        zoomSpeedDecrInterpolator.setEndValue(1);
        zoomSpeedDecrInterpolator.setSpeed(15);
        zoomSpeedDecrInterpolator.start();*/

        zoomInterpolator = new LinearInterpolator();
        zoomInterpolator.setStartValue(defaultZoom);
        zoomInterpolator.setEndValue(endZoom/*defaultZoom * 0.68*/);
        zoomInterpolator.setSpeed(1);
        zoomInterpolator.start();
    }

    private void addLog(String mes) {
        Log.e(getClass().getSimpleName(), mes);
    }

    private double endZoom;

    public double getEndZoom() {
        return endZoom;
    }

    public void setEndZoom(double endZoom) {
        this.endZoom = endZoom;
    }

    private void waitForFinish() {
        animationState = AnimationState.WaitForFinish;

        waitFunction = new WaitFunction();
        waitFunction.setWaitTime(3);
        waitFunction.start();
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    public void finishZoomInterpolator() {
        zoomInterpolator.setFinished(true);
    }

    private double calculateStartZoomSpeed() {
        double speed = Math.abs(defaultZoom - endZoom) / 0.96;

        if (speed >= 2.5)
            speed = 2.5;
        if (speed <= 1)
            speed = 1;

        return Math.max(speed, 1);
    }


    public List<Vector> deleteRedundantPointsInCentralPath(List<Vector> centralPathList, Layer green) {

        List<Vector> updatedList = new ArrayList<>();
        Vector point;

        for (int i = 0; i < centralPathList.size() - 1; i++) {
            point = centralPathList.get(i);
            if (!greenContainsRedundantPoint(point, green.getExtremeBox()))
                updatedList.add(point);

        }

        updatedList.add(centralPathList.get(centralPathList.size() - 1));

        return updatedList;

    }

    public boolean greenContainsRedundantPoint(Vector point, List<Vector> points) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if ((points.get(i).y > point.y) != (points.get(j).y > point.y) &&
                    (point.x < (points.get(j).x - points.get(i).x) * (point.y - points.get(i).y) / (points.get(j).y - points.get(i).y) + points.get(i).x)) {
                result = !result;
            }
        }
        return result;
    }

}
