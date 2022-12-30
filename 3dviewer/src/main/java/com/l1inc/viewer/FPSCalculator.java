package com.l1inc.viewer;

/**
 * Created by Yevhen Paschenko on 2/24/2016.
 */
public class FPSCalculator {

	private int numFrames;
	private int fps;
	private long lastUpdateTime;

	public int getFps() {
		return fps;
	}

	public void frame() {
		numFrames++;

		long timeMillis = System.currentTimeMillis();
		if (timeMillis - lastUpdateTime > 1000) {
			fps = numFrames;
			numFrames = 0;
			lastUpdateTime = timeMillis;
		}
	}
}
