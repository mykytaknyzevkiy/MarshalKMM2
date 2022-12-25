package com.l1inc.viewer.drawing;

import android.graphics.Color;
import android.opengl.GLES20;

import com.l1inc.viewer.Course3DRenderer;

import java.nio.FloatBuffer;

/**
 * Created by Kirill Kartukov on 03.11.2017.
 */

public class BaseDrawingObject {

    protected int textureId = -1;

    protected void drawObjectWithArrays(Course3DRenderer renderer, int mode, int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer, int count, float opacity) {
        drawArraysWithTexture(renderer, mode, textureId, vertexBuffer, textureBuffer, count, opacity);
    }

    protected void drawObjectWithElements(Course3DRenderer renderer, int mode, int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer, java.nio.Buffer indexBuffer, int count, float opacity) {
        drawElementsWithTexture(renderer, mode, textureId, vertexBuffer, textureBuffer, indexBuffer, count, opacity);
    }

    protected void drawLines(Course3DRenderer renderer, FloatBuffer buffer, int count, int size, int mode, Integer customColor) {
        GLES20.glUniform1f(renderer.isTexture, 0);
        if (customColor == null)
            GLES20.glUniform4f(renderer.vColor, 1.0f, 0.0f, 0.0f, 1.0f);
        else
            GLES20.glUniform4f(renderer.vColor, Color.red(customColor) / 255f, Color.green(customColor) / 255f, Color.blue(customColor) / 255f, 1.0f);
        GLES20.glVertexAttribPointer(renderer.aPositionLocation, size, GLES20.GL_FLOAT, false, 0, buffer);
        GLES20.glEnableVertexAttribArray(renderer.aPositionLocation);
        GLES20.glDrawArrays(mode, 0, count);
        if (customColor == null)
            GLES20.glUniform4f(renderer.vColor, 1.0f, 1.0f, 1.0f, 1.0f);

        buffer.clear();

    }

    private void drawArraysWithTexture(Course3DRenderer renderer, int mode, int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer, int count, float opacity) {
        if (vertexBuffer == null || textureBuffer == null || renderer == null)
            return;
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(renderer.texSampler, 0);
        GLES20.glUniform1f(renderer.opacity, opacity);
        GLES20.glUniform1f(renderer.isTexture, 1);
        GLES20.glVertexAttribPointer(renderer.aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(renderer.vTexCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(renderer.vTexCoord);
        GLES20.glEnableVertexAttribArray(renderer.aPositionLocation);
        GLES20.glDrawArrays(mode, 0, count);
        vertexBuffer.clear();
        textureBuffer.clear();
    }

    private void drawElementsWithTexture(Course3DRenderer renderer, int mode, int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer, java.nio.Buffer indexBuffer, int count, float opacity) {
        if (vertexBuffer == null || textureBuffer == null || indexBuffer == null)
            return;
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(renderer.texSampler, 0);
        GLES20.glUniform1f(renderer.opacity, opacity);
        GLES20.glUniform1f(renderer.isTexture, 1);
        GLES20.glVertexAttribPointer(renderer.aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(renderer.vTexCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(renderer.vTexCoord);
        GLES20.glEnableVertexAttribArray(renderer.aPositionLocation);
        GLES20.glDrawElements(mode, count, indexBuffer instanceof FloatBuffer ? GLES20.GL_UNSIGNED_INT : GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        vertexBuffer.clear();
        textureBuffer.clear();
        indexBuffer.clear();
    }

    private void drawGroundElementsWithTexture(Course3DRenderer renderer, int mode, int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer, java.nio.Buffer indexBuffer, int count, float opacity) {
        if (vertexBuffer == null || textureBuffer == null || indexBuffer == null)
            return;
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(renderer.texSampler, 0);
        GLES20.glUniform1f(renderer.opacity, opacity);
        GLES20.glUniform1f(renderer.isTexture, 1);
        GLES20.glVertexAttribPointer(renderer.aPositionLocation, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(renderer.vTexCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(renderer.vTexCoord);
        GLES20.glEnableVertexAttribArray(renderer.aPositionLocation);
        GLES20.glDrawElements(mode, count, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        vertexBuffer.clear();
        textureBuffer.clear();
        indexBuffer.clear();
    }

}
