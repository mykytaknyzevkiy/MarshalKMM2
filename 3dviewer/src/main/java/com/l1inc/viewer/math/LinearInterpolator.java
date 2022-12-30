package com.l1inc.viewer.math;

/**
 * Created by Yevhen Paschenko on 4/14/2016.
 */
public class LinearInterpolator {

    private double startValue;
    private double endValue;
    private double currentValue;
    private double speed;
    private boolean finished;
    private int directionFactor;
    private boolean zoomChanged = false;


    public double getStartValue() {
        return startValue;
    }

    public void setStartValue(double startValue) {
        this.startValue = startValue;
    }

    public double getEndValue() {
        return endValue;
    }

    public void setEndValue(double endValue) {
        this.endValue = endValue;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
        startValue = endValue;
    }

    public void start() {
        currentValue = startValue;
        finished = false;
        directionFactor = startValue < endValue ? 1 : -1;
    }

    public double getCompletePercentage() {
        if (startValue == endValue)
            return 1;
        return directionFactor == 1 ? (currentValue - startValue) / (endValue - startValue) : (startValue - currentValue) / (startValue - endValue);
    }

    public double getCurrentValueAtPercentage(double completePercentage) {
        if (completePercentage >= 1)
            return endValue;

        if (directionFactor == 1) {
            double diff = endValue - startValue;
            double diffCp = diff * completePercentage;
            return startValue + diffCp;
        } else {
            double diff = startValue - endValue;
            double diffCp = diff * completePercentage;
            return startValue - diffCp;
        }
    }

    public void changeDirectionFactor() {
        if (zoomChanged)
            return;

        directionFactor = directionFactor == 1 ? -1 : 1;
        zoomChanged = true;
    }

    public void tick(final long timeElapsed) {
        if (finished) {
            return;
        }

        currentValue += timeElapsed / 1000.0 * speed * directionFactor;
        if (directionFactor == 1 && currentValue > endValue ||
                directionFactor == -1 && currentValue < endValue) {
            currentValue = endValue;
            finished = true;
        }
    }

    public int getReverseInt(int value) {
        int resultNumber = 0;
        for (int i = value; i != 0; i /= 10) {
            resultNumber = resultNumber * 10 + i % 10;
        }
        return resultNumber;
    }
}
