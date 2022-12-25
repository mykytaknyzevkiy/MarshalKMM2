package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.math.VectorMath;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Yevhen Paschenko on 4/21/2016.
 */
public class Sky extends BaseDrawingObject {

	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private int textureId;
	private int vertexCount;
	private long lastTimeMillis;
	private float uoffset;
	private boolean enableAnimation = false;

	public Sky(int resId, Context context) {
		List<Float> vertexList = new ArrayList<>();

		double currentAngle = 0;

		while (currentAngle < Math.PI * 2) {
			float x = (float) (Math.cos(currentAngle) * Constants.SCENE_RADIUS);
			float y = (float) (Math.sin(currentAngle) * Constants.SCENE_RADIUS);
			vertexList.add(x);
			vertexList.add(y);
			vertexList.add(-250f);

			vertexList.add(x);
			vertexList.add(y);
			vertexList.add(2000f);

			currentAngle += VectorMath.deg2rad(Constants.SCENE_ANGLE_STEP);
		}

		buildUV();

//		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);

		vertexCount = vertexList.size();
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		for (Float val : vertexList) {
			vertexBuffer.put(val);
		}
		vertexBuffer.position(0);

		textureId = TextureCache.getTexture(context, resId);

//		int textures[] = new int[1];
//		GLES20.glGenTextures(1, textures, 0);
//
//		textureId = textures[0];
//
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
//		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//		bitmap.recycle();
	}

	public void draw(Course3DRenderer renderer) {
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		drawObjectWithArrays(renderer,GLES20.GL_TRIANGLE_STRIP,textureId,vertexBuffer,textureBuffer,vertexCount/3,1);
		vertexBuffer.clear();
		textureBuffer.clear();

	}

	public void destroy() {
		GLES20.glDeleteTextures(1, new int[]{textureId}, 0);

		TextureCache.logDeallocatedTexture(textureId);
	}

	// TODO: mem usage optimization
	private void buildUV() {
		List<Float> uvList = new ArrayList<>();
		double currentAngle = 0;
		while (currentAngle < Math.PI * 2) {
			float u = (float) (currentAngle / Math.PI) + uoffset;
			uvList.add(u);
			uvList.add(1f);

			uvList.add(u);
			uvList.add(0f);

			currentAngle += VectorMath.deg2rad(Constants.SCENE_ANGLE_STEP);
		}
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(uvList.size() * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		for (Float val : uvList) {
			textureBuffer.put(val);
		}
		textureBuffer.position(0);
	}
}
