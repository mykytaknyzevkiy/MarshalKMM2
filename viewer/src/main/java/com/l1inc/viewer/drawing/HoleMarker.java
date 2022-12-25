package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.Matrix;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.math.Vector;
import com.l1inc.viewer.R;


/**
 * Created by Yevhen Paschenko on 6/13/2016.
 */
public class HoleMarker {

    public static final float DEFAULT_SCALE = 6.0f;

    private IndexedTexturedSquare dotMarker;
    private IndexedTexturedSquare textMarker;
    private Integer number;
    private Vector position;
    private Typeface typeface;
    private float fontScale = DEFAULT_SCALE;

    public HoleMarker(final Context context,
                      final Typeface typeface,
                      final Integer number,
                      final Vector position) {
        this.typeface = typeface;
        this.number = number;
        this.position = position.copy();

        dotMarker = new IndexedTexturedSquare(R.drawable.v3d_course_view_hole_marker, context, 1);

        Bitmap bitmap = generateText();
        textMarker = new IndexedTexturedSquare(String.valueOf(number),typeface, 1.0f);
        bitmap.recycle();
    }

    public void drawGroundMarker(Course3DRenderer renderer,
                                 final float viewAngle,
                                 final float rotationAngle,
                                 final float x,
                                 final float y,
                                 final float z) {

        final float calloutX = (float) position.x;
        final float calloutY = (float) position.y;
        Matrix.setIdentityM(renderer.modelViewMatrix, 0);
        Matrix.translateM(renderer.modelViewMatrix, 0, 0, 0, z);
        Matrix.rotateM(renderer.modelViewMatrix, 0, rotationAngle, 0, 0, 1);
        Matrix.translateM(renderer.modelViewMatrix, 0, x + calloutX, y + calloutY, 0);
        Matrix.rotateM(renderer.modelViewMatrix, 0, -rotationAngle, 0, 0, 1);
        Matrix.scaleM(renderer.modelViewMatrix, 0, 0.2f * fontScale, 0.2f * fontScale, 0.2f * fontScale);
        renderer.bindMatrix();
        dotMarker.draw(renderer, renderer.DEFAULT_OPACITY);
    }

    private Bitmap generateText() {
        Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(0x00000000);

        Paint textPaint = new Paint();
        textPaint.setTextSize(75);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(typeface);
        textPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int xpos = canvas.getWidth() / 2;
        int ypos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
        canvas.drawText(String.valueOf(number), xpos, ypos, textPaint);

        return bitmap;
    }
}
