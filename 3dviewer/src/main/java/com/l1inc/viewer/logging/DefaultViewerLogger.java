package com.l1inc.viewer.logging;

import android.util.Log;

/**
 * Created by Yevhen Paschenko on 5/17/2016.
 */
public class DefaultViewerLogger implements ViewerLogger {

	@Override
	public void error(final Object message) {
		Log.e("Course3DViewer", message.toString());
	}

	@Override
	public void info(final Object message) {
		Log.i("Course3DViewer", message.toString());
	}
}
