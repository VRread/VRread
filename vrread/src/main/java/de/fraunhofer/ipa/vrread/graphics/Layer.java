package de.fraunhofer.ipa.vrread.graphics;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.fraunhofer.ipa.vrread.R;

/**
 * Created by tbf on 23.02.2017.
 */

public class Layer {

	private static final String TAG = Renderer.class.getSimpleName();

	// Store our model data in a float buffer.
	private FloatBuffer wallVertices;
	private ScrollingTextureShader shader;

	/**
	 * Calles when the object is added to the scene.
	 */
	public void onCreated() {

		Log.i(TAG, "onCreated");
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f); // Dark background so text shows up well.

		// make a floor
		ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.PLANE_COORDS.length * 4);
		bbFloorVertices.order(ByteOrder.nativeOrder());
		wallVertices = bbFloorVertices.asFloatBuffer();
		wallVertices.put(WorldLayoutData.PLANE_COORDS);
		wallVertices.position(0);
	}

	public void onDrawEye(float[] modelViewProjection) {
		shader.useShader();
		shader.setModelViewProjection(modelViewProjection);
		shader.setModelVertices(wallVertices);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		GLHelper.checkGLError("drawing floor");
	}
}
