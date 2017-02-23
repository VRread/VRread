package de.fraunhofer.ipa.vrread.graphics;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by tbf on 23.02.2017.
 */

final class GLHelper {

	private static final String TAG = GLHelper.class.getSimpleName();

	/**
	 * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
	 *
	 * @param label Label to report in case of error.
	 */
	public static void checkGLError(String label) {
		int error;
		if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, label + ": glError " + error);
			throw new RuntimeException(label + ": glError " + error);
		}
	}
}
