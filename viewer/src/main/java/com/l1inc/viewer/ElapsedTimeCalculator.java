package com.l1inc.viewer;

import android.os.SystemClock;

/**
 * Created by Yevhen Paschenko on 12/6/2016.
 */

public class ElapsedTimeCalculator {

	private long lastTimeMillis;
	private long timeElapsed;

	public long getTimeElapsed() {
		return timeElapsed;
	}

	public void tick() {
		final long currentTimeMillis = SystemClock.uptimeMillis();

		if (lastTimeMillis != 0) {
			timeElapsed = currentTimeMillis - lastTimeMillis;
		}

		lastTimeMillis = currentTimeMillis;
	}

	public void pause(){
	}


	public void resume() {
		lastTimeMillis = SystemClock.uptimeMillis();
		timeElapsed = 0;
	}

}
