/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.geom.Matrix3;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.platform.GLES20;
import gov.nasa.worldwind.platform.Platform;
import gov.nasa.worldwind.util.Logger;

import java.util.ResourceBundle;

// TODO Try accumulating surface tile state (texCoordMatrix, texSampler), loading uniforms once, then loading a uniform
// TODO index to select the state for a surface tile. This reduces the uniform calls when many surface tiles intersect
// TODO one terrain tile.
// TODO Try class representing transform with a specific scale+translate object that can be uploaded to a GLSL vec4
public class SurfaceTextureProgram extends ShaderProgram {

    public static final Object KEY = SurfaceTextureProgram.class;

    public Matrix4 mvpMatrix = new Matrix4();

    public Matrix3[] texCoordMatrix = {new Matrix3(), new Matrix3()};

    protected int enablePickModeId;

    protected int enableTextureId;

    protected int mvpMatrixId;

    protected int texCoordMatrixId;

    protected int texSamplerId;

    protected int colorId;

    private float[] mvpMatrixArray = new float[16];

    private float[] texCoordMatrixArray = new float[9 * 2];

    private Color color = new Color();

    public SurfaceTextureProgram(ResourceBundle resources) {
        try {
            String vs = resources.getString("raw.gov_nasa_worldwind_surfacetextureprogram_vert");
            String fs = resources.getString("raw.gov_nasa_worldwind_surfacetextureprogram_frag");
            this.setProgramSources(vs, fs);
            this.setAttribBindings("vertexPoint", "vertexTexCoord");
        } catch (Exception logged) {
            Logger.logMessage(Logger.ERROR, "SurfaceTextureProgram", "constructor", "errorReadingProgramSource", logged);
        }
    }

    protected void initProgram(DrawContext dc) {
        GLES20 gl = Platform.getGL();

        this.enablePickModeId = gl.glGetUniformLocation(this.programId, "enablePickMode");
        gl.glUniform1i(this.enablePickModeId, 0); // disable pick mode

        this.enableTextureId = gl.glGetUniformLocation(this.programId, "enableTexture");
        gl.glUniform1i(this.enableTextureId, 0); // disable texture

        this.mvpMatrixId = gl.glGetUniformLocation(this.programId, "mvpMatrix");
        new Matrix4().transposeToArray(this.mvpMatrixArray, 0); // 4 x 4 identity matrix
        gl.glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.mvpMatrixArray, 0);

        this.texCoordMatrixId = gl.glGetUniformLocation(this.programId, "texCoordMatrix");
        new Matrix3().transposeToArray(this.texCoordMatrixArray, 0); // 3 x 3 identity matrix
        new Matrix3().transposeToArray(this.texCoordMatrixArray, 9); // 3 x 3 identity matrix
        gl.glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.texCoordMatrixArray, 0);

        this.colorId = gl.glGetUniformLocation(this.programId, "color");
        this.color.set(1, 1, 1, 1); // opaque white
        gl.glUniform4f(this.colorId, this.color.red, this.color.green, this.color.blue, this.color.alpha);

        this.texSamplerId = gl.glGetUniformLocation(this.programId, "texSampler");
        gl.glUniform1i(this.texSamplerId, 0); // GL_TEXTURE0
    }

    public void enablePickMode(boolean enable) {
        Platform.getGL().glUniform1i(this.enablePickModeId, enable ? 1 : 0);
    }

    public void enableTexture(boolean enable) {
        Platform.getGL().glUniform1i(this.enableTextureId, enable ? 1 : 0);
    }

    public void loadModelviewProjection() {
        this.mvpMatrix.transposeToArray(this.mvpMatrixArray, 0);
        Platform.getGL().glUniformMatrix4fv(this.mvpMatrixId, 1, false, this.mvpMatrixArray, 0);
    }

    public void loadTexCoordMatrix() {
        this.texCoordMatrix[0].transposeToArray(this.texCoordMatrixArray, 0);
        this.texCoordMatrix[1].transposeToArray(this.texCoordMatrixArray, 9);
        Platform.getGL().glUniformMatrix3fv(this.texCoordMatrixId, 2, false, this.texCoordMatrixArray, 0);
    }

    public void loadColor(Color color) {
        if (!this.color.equals(color)) { // suppress unnecessary writes to GLSL uniform variables
            this.color.set(color);
            float a = color.alpha;
            Platform.getGL().glUniform4f(this.colorId, color.red * a, color.green * a, color.blue * a, a);
        }
    }
}
