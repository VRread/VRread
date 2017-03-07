package de.fraunhofer.ipa.vrread.graphics.layer;

import android.opengl.GLES20;
import android.util.Log;

import com.google.vr.sdk.base.HeadTransform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Objects;

import de.fraunhofer.ipa.vrread.datasource.TextureSize;
import de.fraunhofer.ipa.vrread.graphics.Renderer;
import de.fraunhofer.ipa.vrread.graphics.WorldLayoutData;
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
	 * @return The shader of the layer.
	 */
	public QuadShader getShader() {
		return shader;
	}

	/**
	 * Gets the current best texture size.
	 *
	 * @return The best texture size for this platform.
	 */
	public TextureSize getTextureSize() {
		// This should be determined by opengl.
		return new TextureSize(1024, 1024);
	}

	/**
	 * Calls when the object is added to the scene.
	 */
	public void onCreated() {
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
	public void onDrawEye(float[] modelViewProjection) {

		if (shader != null) {
			shader.useShader();
			shader.setModelViewProjection(modelViewProjection);
			shader.setModelVertices(quadVertices);
		}

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		GLHelper.checkGLError(GL_ERROR_TAG);
	}

	/**
	 * Called before the eyes are drawn next.
	 *
	 * @param headTransform The new head tranform.
	 */
	public void onNewFrame(HeadTransform headTransform) {
		// no op.
	}
}
