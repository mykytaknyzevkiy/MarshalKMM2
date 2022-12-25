package com.l1inc.viewer.math;

/**
 * Created by Yevhen Paschenko on 4/15/2016.
 */
public class WaitFunction {

	private double waitTime;
	private boolean finished;
	private double currentTime;

	public double getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(double waitTime) {
		this.waitTime = waitTime;
	}

	public boolean isFinished() {
		return finished;
	}

	public void start() {
		currentTime = 0;
		finished = false;
	}

	public void tick(final long timeElapsed) {
		if (finished) {
			return;
		}

		currentTime += timeElapsed / 1000.0;
		if (currentTime >= waitTime) {
			finished = true;
		}
	}


}
