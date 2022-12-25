package com.l1inc.viewer;

import android.content.Context;
import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;

/**
 * Created by Kirill Kartukov on 09.10.2017.
 */

public class ShaderUtils {

    public static class GLProgram {
        private int[] shaderIds;
        private int programId;

        public int getProgramId() {
            return programId;
        }

        public void destroy() {
            for (final int id : shaderIds) {
                if (id != 0) {
                    glDeleteShader(id);
                }
            }

            if (programId != 0) {
                glDeleteProgram(programId);
            }
        }
    }

    public static GLProgram createProgram(int... vertexShaderId) {
        final int programId = glCreateProgram();

        final GLProgram glProgram = new GLProgram();

        if (programId == 0) {
            addLog(" create program  failed for ");
            return glProgram;
        }

        glProgram.programId = programId;

        glProgram.shaderIds = new int[vertexShaderId.length];
        for (int i = 0 ; i < vertexShaderId.length ; i++) {
            glAttachShader(programId, vertexShaderId[i]);
            glProgram.shaderIds[i] = vertexShaderId[i];
        }

        glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            glDeleteProgram(programId);
            glProgram.programId = 0;
            addLog(" create program  failed fo   222r ");
            return glProgram;
        }

        return glProgram;
    }

    public static int createShader(Context context, int type, int shaderRawId) {
        String shaderText = FileUtils.readTextFromRaw(context, shaderRawId);
        return ShaderUtils.createShader(type, shaderText);
    }

    public static int createShader(int type, String shaderText) {
        final int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            addLog(" createShader failed for " +shaderText);
            return 0;
        }
        glShaderSource(shaderId, shaderText);
        glCompileShader(shaderId);
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            glDeleteShader(shaderId);
            addLog(" 222  createShader failed for " +shaderText);
            return 0;
        }
        return shaderId;
    }

    private static void addLog(String mes){
        Log.e("Course3DRenderer", mes);
    }
}
