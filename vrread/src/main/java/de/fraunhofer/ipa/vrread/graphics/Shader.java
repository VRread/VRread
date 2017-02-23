package de.fraunhofer.ipa.vrread.graphics;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

/**
 * Created by tbf on 23.02.2017.
 */

public abstract class Shader {

	private static final String TAG = Shader.class.getSimpleName();

	public abstract void useShader();
	public abstract void loadShader();

	public abstract void setModelViewProjection(float[] mvp);

	public abstract void setModelVertices(FloatBuffer vertices);

	/**
	 * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
	 *
	 * @param shaderCode Contains the code of the shader to be used.
	 * @param type  The type of shader we will be creating.
	 * @return The shader object handler.
	 */
	protected int loadGLShader(String shaderCode, int type) {
		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		// Get the compilation status.
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

		// If the compilation failed, delete the shader.
		if (compileStatus[0] == 0) {
			Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}

		if (shader == 0) {
			throw new RuntimeException("Error creating shader.");
		}

		return shader;
	}
}
