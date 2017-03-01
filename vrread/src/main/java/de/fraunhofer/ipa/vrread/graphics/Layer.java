package de.fraunhofer.ipa.vrread.graphics;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Objects;

import de.fraunhofer.ipa.vrread.graphics.shader.GLHelper;
import de.fraunhofer.ipa.vrread.graphics.shader.QuadShader;


/**
 * Creates a simple geometry from vertices.
 * <p>
 * Created by Thomas Felix on 23.02.2017.
 */

public class Layer {

	private static final String TAG = Renderer.class.getSimpleName();
	private static final String GL_ERROR_TAG = TAG + " draw quad";

	private final QuadShader shader;

	// Store our model data in a float buffer.
	@SuppressWarnings("WeakerAccess")
	protected FloatBuffer quadVertices;

	/**
	 * Use this ctor to set internal shader to null. Then the subsclass has to manage the shader handling and
	 * calling by
	 * itself.
	 */
	Layer() {

		this.shader = null;
	}

	Layer(QuadShader shader) {

		this.shader = Objects.requireNonNull(shader);
	}

	/**
	 * Calls when the object is added to the scene.
	 */
	void onCreated() {
		Log.i(TAG, "onCreated");

		// make a floor
		ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.PLANE_COORDS.length * 4);
		bbFloorVertices.order(ByteOrder.nativeOrder());
		quadVertices = bbFloorVertices.asFloatBuffer();
		quadVertices.put(WorldLayoutData.PLANE_COORDS);
		quadVertices.position(0);

		if (shader != null) {
			shader.loadShader();
		}
	}

	/**
	 * Draws a single eye. This basically feeds the model view projection to the shaders.
	 *
	 * @param modelViewProjection The new matrix.
	 */
	void onDrawEye(float[] modelViewProjection) {

		if (shader != null) {
			shader.useShader();
			shader.setModelViewProjection(modelViewProjection);
			shader.setModelVertices(quadVertices);
		}

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		GLHelper.checkGLError(GL_ERROR_TAG);
	}
}
