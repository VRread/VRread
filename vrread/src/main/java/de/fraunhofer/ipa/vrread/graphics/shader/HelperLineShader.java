package de.fraunhofer.ipa.vrread.graphics.shader;

import android.content.Context;
import android.opengl.GLES20;

import de.fraunhofer.ipa.vrread.R;

/**
 * This controls a shader which is able to draw a horizontal helper
 * line in order to make it easier to read the text.
 *
 * Created by tbf on 24.02.2017.
 */

public class HelperLineShader extends QuadShader {

	private Context ctx;

	public HelperLineShader(Context ctx) {

		this.ctx = ctx;
	}

	@Override
	public void useShader() {
		GLES20.glUseProgram(quadProgram);

		// Set the texture uv coordinates.
		GLES20.glVertexAttribPointer(textureCoordinateParam, 2, GLES20.GL_FLOAT, false, 0, wallTextureCoordinates);
		GLES20.glEnableVertexAttribArray(textureCoordinateParam);
	}

	@Override
	protected void createShaderProgram() {
		final int vertexShader = loadGLShader(GLHelper.readRawTextFile(ctx, R.raw.vertex), GLES20.GL_VERTEX_SHADER);
		final int lineShader = loadGLShader(GLHelper.readRawTextFile(ctx, R.raw.helperline), GLES20.GL_FRAGMENT_SHADER);

		quadProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(quadProgram, vertexShader);
		GLES20.glAttachShader(quadProgram, lineShader);
		GLES20.glLinkProgram(quadProgram);
	}
}
