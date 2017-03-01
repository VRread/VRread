package de.fraunhofer.ipa.vrread.graphics.shader;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.fraunhofer.ipa.vrread.graphics.WorldLayoutData;

/**
 * Created by tbf on 24.02.2017.
 */
@SuppressWarnings("WeakerAccess")
public abstract class QuadShader extends Shader {

	protected FloatBuffer wallTextureCoordinates;
	// This will be used to pass in the texture image.
	protected int textureCoordinateParam;

	@SuppressWarnings("WeakerAccess")
	protected int quadProgram;

	private int wallPositionParam;
	private int wallModelViewProjectionParam;

	public QuadShader() {
		// no op.
	}

	protected abstract void createShaderProgram();

	@SuppressWarnings("WeakerAccess")
	protected void checkShaderLinkError() {
		// Error check the linkage.
		// Get the link status.
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(quadProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

		// If the link failed, delete the program.
		if (linkStatus[0] == 0) {
			GLES20.glDeleteProgram(quadProgram);
			quadProgram = 0;
		}
		if (quadProgram == 0) {
			throw new RuntimeException("Error creating program.");
		}

		GLHelper.checkGLError("Floor program");
	}

	@Override
	public void loadShader() {
		// Get the UV coordiantes.
		ByteBuffer bbFloorTexCords = ByteBuffer.allocateDirect(WorldLayoutData.PLANE_TEX_CORDS.length * 4);
		bbFloorTexCords.order(ByteOrder.nativeOrder());
		wallTextureCoordinates = bbFloorTexCords.asFloatBuffer();
		wallTextureCoordinates.put(WorldLayoutData.PLANE_TEX_CORDS);
		wallTextureCoordinates.position(0);

		createShaderProgram();

		checkShaderLinkError();

		GLES20.glUseProgram(quadProgram);

		wallPositionParam = GLES20.glGetAttribLocation(quadProgram, "a_Position");
		textureCoordinateParam = GLES20.glGetAttribLocation(quadProgram, "a_TexCoordinate");
		wallModelViewProjectionParam = GLES20.glGetUniformLocation(quadProgram, "u_MVPMatrix");

		GLHelper.checkGLError("quad shader params");
	}

	@Override
	public void setModelViewProjection(float[] mvp) {
		GLES20.glUniformMatrix4fv(wallModelViewProjectionParam, 1, false, mvp, 0);
	}

	@Override
	public void setModelVertices(FloatBuffer vertices) {
		// Set the position of the model
		GLES20.glVertexAttribPointer(wallPositionParam, 3, GLES20.GL_FLOAT, false, 0, vertices);
		GLES20.glEnableVertexAttribArray(wallPositionParam);
	}
}
