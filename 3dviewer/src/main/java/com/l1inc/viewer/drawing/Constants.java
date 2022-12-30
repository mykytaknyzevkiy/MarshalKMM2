package com.l1inc.viewer.drawing;

/**
 * Created by Yevhen Paschenko on 4/21/2016.
 */
public class Constants {

	public static final int SCENE_RADIUS = 1000;
	public static final int SCENE_ANGLE_STEP = 10; // degrees
	public static final int LOCATION_SCALE = 20;
	public static final Float CAMERA_MAX_VALUE = 5000f;
	public static final float CREEK_OLD_SCALE = 0.1f;
	public static final float GREEN_OLD_SCALE = 0.05f;
	public static final int LOCATION_OLD_SCALE = 20;
	public static final float SCALE_01 = LOCATION_SCALE * CREEK_OLD_SCALE / LOCATION_OLD_SCALE;
	public static final float SCALE_005 = LOCATION_SCALE * GREEN_OLD_SCALE / LOCATION_OLD_SCALE;
}
