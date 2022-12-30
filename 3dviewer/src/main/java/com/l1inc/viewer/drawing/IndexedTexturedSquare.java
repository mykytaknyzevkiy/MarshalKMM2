package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;

import com.l1inc.viewer.Course3DRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


/**
 * Created by Yevhen Paschenko on 2/21/2016.
 */
public class IndexedTexturedSquare extends BaseDrawingObject {

    private enum SquareType {
        HOLE_MARKER,
        T_WITH_TEXT,
        C_WITH_TEXT,
        SIMPLE,
        DISTANCE_MARKER
    }

    private SquareType squareType = SquareType.SIMPLE;

    private float tiles = 1f;
    private boolean callout = false;
    //HOLE MARKER
    private String text;
    private int textD;
    private Typeface typeface;
    //T_WITH_TEXT
    private Context context;
    private int resId;
    private int color;
    private float param;
    private boolean recycle;
    //C_WITH_TEXT
    private Bitmap calloutbg = null;


    public IndexedTexturedSquare(float tiles) {
        //TODO TILES IS ALWAYS 1
        this.squareType = SquareType.DISTANCE_MARKER;
    }

    public IndexedTexturedSquare(String text, Typeface typeface, float tiles) {
        this.text = text;
        this.typeface = typeface;
        this.squareType = SquareType.HOLE_MARKER;
        textureId = TextureCache.getHoleMarkerTexture(text, typeface, false, GLES20.GL_CLAMP_TO_EDGE);
    }

    public IndexedTexturedSquare(Context context, int resId, Typeface typeface, int color, float param, float tiles, boolean recycle, boolean callout) {
        this.context = context;
        this.resId = resId;
        this.typeface = typeface;
        this.color = color;
        this.param = param;
        this.recycle = recycle;
        this.squareType = SquareType.T_WITH_TEXT;
        textureId = TextureCache.getTextureWithText(context, resId, color, typeface, recycle, param);
    }

    public IndexedTexturedSquare(Context context,String key, String text, Typeface typeface, int color, float param, float tiles, boolean recycle, boolean callout) {
        this.context = context;
        this.resId = resId;
        this.typeface = typeface;
        this.color = color;
        this.param = param;
        this.recycle = recycle;
        this.squareType = SquareType.T_WITH_TEXT;
        textureId = TextureCache.getTextureWithText(context, resId, color, typeface, recycle, param);
    }

    public IndexedTexturedSquare(int resId, String text, Bitmap calloutbg, Typeface typeface, int color, float param, float tiles, boolean recycle, boolean callout) {
        this.resId = resId;
        this.text = text;
        this.typeface = typeface;
        this.color = color;
        this.param = param;
        this.recycle = recycle;
        this.calloutbg = calloutbg;
        this.squareType = SquareType.C_WITH_TEXT;
        textureId = TextureCache.getCalloutWithText(resId, text, calloutbg, color, typeface, recycle, param);
    }

    public IndexedTexturedSquare(int resId, String text, Bitmap calloutbg, Typeface typeface, int color, float param) {
        this.resId = resId;
        this.text = text;
        this.typeface = typeface;
        this.color = color;
        this.param = param;
        this.recycle = recycle;
        this.calloutbg = calloutbg;
        this.squareType = SquareType.C_WITH_TEXT;
        textureId = TextureCache.getCartMarkerWithText(resId, text, calloutbg, color, typeface, true, param);
    }

    public void updateTexture(int resId, String text, Bitmap calloutbg, Typeface typeface, int color, float param, float tiles, boolean recycle) {
        this.resId = resId;
        this.text = text;
        this.typeface = typeface;
        this.color = color;
        this.param = param;
        this.recycle = recycle;
        this.calloutbg = calloutbg;
        this.squareType = SquareType.C_WITH_TEXT;
        textureId = TextureCache.getCalloutWithText(resId, text, calloutbg, color, typeface, recycle, param);
    }

    public IndexedTexturedSquare(int resId, Context context, float tiles) {
        this.resId = resId;
        this.context = context;
        this.squareType = SquareType.SIMPLE;
        textureId = TextureCache.getTexture(context, resId, false, GLES20.GL_CLAMP_TO_EDGE);
    }

    public void draw(Course3DRenderer renderer, float opacityValue) {
        reloadTextureIfNeeded();
        drawObjectWithElements(renderer, GLES20.GL_TRIANGLES, textureId, renderer.vertexBuffer, renderer.textureBuffer, renderer.indexBuffer, renderer.indices.length, opacityValue);
    }

    public void drawArrays(Course3DRenderer renderer, float opacityValue) {
        reloadTextureIfNeeded();
        drawObjectWithArrays(renderer, GLES20.GL_TRIANGLES, textureId, renderer.vertexBufferTree, renderer.textureBufferTree, 12, opacityValue);
    }

    public void destroy() {
        //destroyTexture();
        //TextureCache.destroyById(textureId);
    }

    public void updateTexture(int tId, int text, Typeface typeface) {
        this.textureId = tId;
        this.textD = text;
        this.typeface = typeface;
        textureId = TextureCache.updateTexture(textureId, text, typeface, false, GLES20.GL_CLAMP_TO_EDGE);
    }

    private void reloadTextureIfNeeded() {
        try {
            if (textureId == -1 || !TextureCache.hasTexture(resId, textureId)) {
                switch (squareType) {
                    case SIMPLE:
                        textureId = TextureCache.getTexture(context, resId, false, GLES20.GL_CLAMP_TO_EDGE);
                        break;
                    case C_WITH_TEXT:
                        textureId = TextureCache.getCalloutWithText(resId, text, calloutbg, color, typeface, recycle, param);
                        break;
                    case T_WITH_TEXT:
                        textureId = TextureCache.getTextureWithText(context, resId, color, typeface, recycle, param);
                        break;
                    case HOLE_MARKER:
                        textureId = TextureCache.getHoleMarkerTexture(text, typeface, false, GLES20.GL_CLAMP_TO_EDGE);
                        break;
                    case DISTANCE_MARKER:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
