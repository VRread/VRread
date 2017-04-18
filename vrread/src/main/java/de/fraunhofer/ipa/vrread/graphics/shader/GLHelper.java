package de.fraunhofer.ipa.vrread.graphics.shader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by tbf on 23.02.2017.
 */
public final class GLHelper {

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

	/**
	 * Converts a raw text file into a string.
	 *
	 * @param resId The resource ID of the raw text file about to be turned into a shader.
	 * @param ctx   Android context to access the ressources.
	 * @return The context of the text file, or null in case of error.
	 */
	static String readRawTextFile(Context ctx, int resId) {
		InputStream inputStream = ctx.getResources().openRawResource(resId);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
