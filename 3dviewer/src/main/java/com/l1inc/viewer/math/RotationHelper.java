package com.l1inc.viewer.math;

/**
 * Created by Yevhen Paschenko on 4/13/2016.
 */
public class RotationHelper {

	private double speed;
	private double endValue;
	private double currentValue = Double.MIN_VALUE;
	private double directionFactor;

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getEndValue() {
		return endValue;
	}

	public void setEndValue(double endValue) {
		endValue = normalizeAngle(endValue);
		if (currentValue == Double.MIN_VALUE) {
			currentValue = endValue;
		} else {
			double diff1 = Math.abs(currentValue - endValue);
			double diff2 = Math.abs(currentValue - (endValue + 360));
			double diff3 = Math.abs((currentValue + 360) - endValue);
			if (diff2 < diff1) {
				endValue += 360;
			} else if (diff3 < diff1) {
				currentValue += 360;
			}
		}

		directionFactor = currentValue < endValue ? 1 : -1;
		this.endValue = endValue;
	}

	public double getCurrentValue() {
		return currentValue;
	}

	public void tick(final long timeElapsed) {
		if (currentValue == endValue) {
			return;
		}

		double rotationFactor = 1 + Math.abs(endValue - currentValue) / 3.0;
		currentValue += timeElapsed / 1000.0 * speed * directionFactor * rotationFactor;

		if (directionFactor > 0 && currentValue > endValue) {
			currentValue = normalizeAngle(endValue);
		} else if (directionFactor < 0 && currentValue < endValue) {
			currentValue = normalizeAngle(endValue);
		}
	}

	public static double normalizeAngle(double angle) {
		while (angle < 0) {
			angle += 360;
		}
		angle = angle % 360;
		return angle;
	}
}
