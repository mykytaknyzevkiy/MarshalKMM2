package com.l1inc.viewer.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.l1inc.viewer.Course3DRenderer;
import com.l1inc.viewer.Course3DViewer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Created by Yevhen Paschenko on 6/9/2016.
 */
public class TextureCache extends BaseDrawingObject {

    public static List<Integer> allocatedTextures = new ArrayList<>();

    private static Map<Integer, Integer> cache = new HashMap<>();
    private static ArrayList<Integer> keysToRemove = new ArrayList<>();
    private static int customDrawableId = 0;
    private static final int maxCalloutsTextureCount = 20;
    private static  int curentCalloutsTextureCount = 0;

    public static synchronized int getNumTextures() {
        return cache.keySet().size();
    }

    public static void logAllocatedTexture(final Integer textureId) {

//        final StringBuilder stringBuilder = new StringBuilder();
//        for (final StackTraceElement element : Thread.currentThread().getStackTrace()) {
//            stringBuilder.append(element);
//            stringBuilder.append("\n");
//        }
//
//        allocatedTextures.add(textureId);
//        Course3DRenderer.getViewerLogger().info("allocated texture id " + textureId + " inmemory textures count " + allocatedTextures.size() +  " cache size is " + cache.keySet().size() + " stack: " + stringBuilder.toString());
    }

    public static void logDeallocatedTexture(final Integer textureId) {

//        final StringBuilder stringBuilder = new StringBuilder();
//        for (final StackTraceElement element : Thread.currentThread().getStackTrace()) {
//            stringBuilder.append(element);
//            stringBuilder.append("\n");
//        }
//
//        TextureCache.allocatedTextures.remove(Integer.valueOf(textureId));
//        Course3DRenderer.getViewerLogger().info("deleted texture id " + textureId + " inmemory textures count " + TextureCache.allocatedTextures.size() + " cache size is " + cache.keySet().size() + " stack: " + stringBuilder.toString());
    }

    public static synchronized Integer getTexture(final Context context,
                                     final Integer resourceId) {

        if (!cache.containsKey(resourceId)) {
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            logAllocatedTexture(textures[0]);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();

            cache.put(resourceId, textureId);
        }

        return cache.get(resourceId);
    }

    public static synchronized Integer getTexture(final Context context,
                                     final Integer resourceId,
                                     boolean recycle,
                                     float param) {
        if (!cache.containsKey(resourceId)) {
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            logAllocatedTexture(textures[0]);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, param);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, param);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            //if (recycle)
                bitmap.recycle();

            cache.put(resourceId, textureId);
        }

        return cache.get(resourceId);
    }

    public static synchronized Integer getCompressedTexture(final Context context,
                                     final Integer resourceId,
                                     boolean recycle,
                                     float param) {
        if (!cache.containsKey(resourceId)) {
            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            logAllocatedTexture(textures[0]);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, param);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, param);

            final InputStream inputStream = context.getResources().openRawResource(resourceId);
            try {
                ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D,0,0,GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5,inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            cache.put(resourceId, textureId);
        }

        return cache.get(resourceId);
    }

    public static synchronized Integer getCompressedTexture(final Context context,
                                               final Integer resourceId) {
        if (!cache.containsKey(resourceId)) {
            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            logAllocatedTexture(textures[0]);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            try {
            final InputStream inputStream = context.getResources().openRawResource(resourceId);
                ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D,0,0,GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5,inputStream);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }



            cache.put(resourceId, textureId);
        }

        return cache.get(resourceId);
    }

    private final static Bitmap bitmap_128_128 = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_4444);
    private static Canvas canvas_128_128 = new Canvas(bitmap_128_128);
    private static Paint textPaint = new Paint();
    private static Paint strokePaint = new Paint();

    public static synchronized boolean hasTexture(int resourseId,int textureId){
        return cache!=null && cache.containsKey(resourseId) && cache.get(resourseId) == textureId;
    }

    public static synchronized Integer updateTexture(int customId, final int dist,
                                        final Typeface typeface,
                                        boolean recycle,
                                        float param) {
        if (!cache.containsKey(customId)) {

            textPaint.setTextSize(100);
            textPaint.setAntiAlias(true);
            textPaint.setTypeface(typeface);
            textPaint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
            textPaint.setTextAlign(Paint.Align.CENTER);

            strokePaint.setTextSize(100);
            strokePaint.setAntiAlias(true);
            strokePaint.setTypeface(typeface);
            strokePaint.setARGB(0xFF, 0x00, 0x00, 0x00);
            strokePaint.setTextAlign(Paint.Align.CENTER);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(1);

        }else {
            destroyById(customId);
        }

        bitmap_128_128.eraseColor(0x00000000);
        int xpos = canvas_128_128.getWidth() / 2;
        int ypos = (int) ((canvas_128_128.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
        canvas_128_128.drawText(String.valueOf(dist), xpos, ypos, textPaint);
        canvas_128_128.drawText(String.valueOf(dist), xpos, ypos, strokePaint);

        int textures[] = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        logAllocatedTexture(textures[0]);

        final int textureId = textures[0];
        if (textureId == 0) {
            final int error = GLES20.glGetError();
            Course3DRenderer.getViewerLogger().error("GLError: " + error);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, param);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, param);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap_128_128, 0);
//        if (recycle)
//            bitmap_128_128.recycle();

        cache.put(customId, textureId);

        return cache.get(customId);
    }

    public static synchronized Integer getTextureWithText(Context context,int resId, int color, Typeface typeface, boolean recycle, float param) {

        if (!cache.containsKey(resId)) {
            Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(0x00000000);
            Paint textPaint = new Paint();
            textPaint.setTextSize(50);
            textPaint.setAntiAlias(true);
            textPaint.setTypeface(typeface);
            textPaint.setColor(color);
            textPaint.setTextAlign(Paint.Align.CENTER);
            int xpos = canvas.getWidth() / 2;
            int ypos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
            canvas.drawText(context.getResources().getString(resId), xpos, ypos, textPaint);

            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            logAllocatedTexture(textures[0]);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, param);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, param);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            //if (recycle)
                bitmap.recycle();

            cache.put(resId, textureId);
        }

        return cache.get(resId);
    }

    public static synchronized Integer getCalloutWithText(int resId,String text,Bitmap callout, int color, Typeface typeface, boolean recycle, float param) {
        if(resId == 0){
            resId = -1000000;
        }
        if(curentCalloutsTextureCount >= maxCalloutsTextureCount) {
            keysToRemove.clear();
            for (Integer key : cache.keySet()) {
                if (key < 0) {
                    keysToRemove.add(key);
                }
            }

            for(int val : keysToRemove) {
                destroyById(val);
            }

            keysToRemove.clear();
            curentCalloutsTextureCount = 0;
        }

        if (!cache.containsKey(resId)) {
            Bitmap bg = Bitmap.createScaledBitmap(callout, (int) (64), (int) (64), true);
            Canvas canvas = new Canvas(bg);
            Paint textPaint = new Paint();
            textPaint.setTextSize(25);
            textPaint.setAntiAlias(true);
            textPaint.setTypeface(typeface);
            textPaint.setColor(Color.BLACK);
            textPaint.setTextAlign(Paint.Align.CENTER);

            int xpos = 64 - canvas.getWidth() / 2;
            int ypos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2) + 2);

            canvas.drawText(text, xpos, ypos, textPaint);

            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            logAllocatedTexture(textures[0]);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, param);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, param);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bg, 0);
            //if (recycle)
            if (callout.getWidth() != 64 || callout.getHeight() != 64) {
                bg.recycle();
            }

            cache.put(resId, textureId);
            curentCalloutsTextureCount++;
        }
        return cache.get(resId);
    }

    public static synchronized Integer getHoleMarkerTexture(String number, Typeface typeface, boolean recycle,
                                               float param) {
        if (!cache.containsKey(Integer.valueOf(number))) {
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

            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            logAllocatedTexture(textures[0]);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, param);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, param);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            //if (recycle)
                bitmap.recycle();

            cache.put(Integer.valueOf(number), textureId);
        }

        return cache.get(Integer.valueOf(number));
    }

    public static Integer getCartMarkerWithText(int resId, String text, Bitmap callout, int color, Typeface typeface, boolean recycle, float param) {
        if (resId == 0) {
            resId = -1000000;
        }
        if (curentCalloutsTextureCount >= maxCalloutsTextureCount) {
            keysToRemove.clear();
            for (Integer key : cache.keySet())
                if (key < 0)
                    keysToRemove.add(key);

            for (int val : keysToRemove)
                destroyById(val);
            keysToRemove.clear();
            curentCalloutsTextureCount = 0;
        }
//        Log.e("TEXCACHE", "getCalloutWithText "+ cache.containsKey(resId));
        if (!cache.containsKey(resId)) {
            Bitmap bg2 = callout.copy(callout.getConfig(), true);
            Bitmap bg = Bitmap.createScaledBitmap(bg2, (int) (128), (int) (128), true);
            Canvas canvas = new Canvas(bg);
            Paint textPaint = new Paint();
            textPaint.setTextSize(50);
            textPaint.setAntiAlias(true);
            textPaint.setTypeface(typeface);
            textPaint.setColor(color);
            textPaint.setTextAlign(Paint.Align.CENTER);

            int xpos = canvas.getWidth() / 2;
            int ypos = (int) (canvas.getHeight() / 2);

            canvas.drawText(text, xpos, ypos, textPaint);

            int textures[] = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            final int textureId = textures[0];
            if (textureId == 0) {
                final int error = GLES20.glGetError();
                Course3DRenderer.getViewerLogger().error("GLError: " + error);
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, param);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, param);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bg, 0);
            if (recycle)
                bg.recycle();
            Log.e("TEXCACHE", "getCalloutWithText textureId " + textureId);
            cache.put(resId, textureId);
            curentCalloutsTextureCount++;
        }
        return cache.get(resId);
    }

    public static synchronized void invalidate() {
        for (final int key : cache.keySet()) {
            final int textureId = cache.get(key);
            GLES20.glDeleteTextures(1, new int[]{textureId}, 0);

            logDeallocatedTexture(textureId);
        }
        cache.clear();
    }

    public static synchronized void destroyById(int resId){
        if (cache.containsKey(resId) == false) {
            return;
        }

        final int textureId = cache.get(resId);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);

        logDeallocatedTexture(textureId);

        cache.remove(resId);
    }

    public static synchronized int getCustomDrawableId(){
        return customDrawableId--;
    }

}
