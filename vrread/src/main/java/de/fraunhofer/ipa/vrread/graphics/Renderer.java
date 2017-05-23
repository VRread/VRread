package de.fraunhofer.ipa.vrread.graphics;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;

import de.fraunhofer.ipa.vrread.control.GestureController;
import de.fraunhofer.ipa.vrread.graphics.layer.Layer;
import de.fraunhofer.ipa.vrread.graphics.shader.GLHelper;

/**
 * This handles all the rendering of the reader app. The renderer will calls and uses openGL for drawing the text layer.
 * Created by tbf on 22.02.2017.
 */
public class Renderer implements GvrView.StereoRenderer {

	private static final String TAG = Renderer.class.getSimpleName();

	private static final int MAX_LAYERS = 10;

	private static final float Z_NEAR = 0.1f;
	private static final float Z_FAR = 100.0f;
	private static final float Z_MODEL_POS = 1.45f; // was 1.3
	private static final float Z_LAYER_DISTANCE = 0.01f;
	private static final float CAMERA_Z = 0.5f;

	/**
	 * Position of the camera.
	 */
	private float[] cameraMatrix = new float[16];

	/**
	 * Positions the model.
	 */
	private float[] modelMatrix = new float[16];

	/**
	 * Projection matrix.
	 */
	private float[] modelViewProjection = new float[16];


	/**
	 * Holds the different render layer.
	 */
	private boolean[] layersInitialized = new boolean[MAX_LAYERS];
	private Layer[] layers = new Layer[MAX_LAYERS];

	private GestureController gestureController;

	public Renderer(GvrView gvrView) {
		gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
		gvrView.setRenderer(this);
		gvrView.setTransitionViewEnabled(true);

		Arrays.fill(layersInitialized, false);
	}

	/**
	 * Sets a gesture controller which is responsible for reading the movement and controlling the environment. Setting
	 * it to null will disable all movement control.
	 *
	 * @param gestureController The new gesture controller of null.
	 */
	public void setGestureController(GestureController gestureController) {
		this.gestureController = gestureController;
	}

	/**
	 * TODO Den Zugriff hier threadsafe machen.
	 * Adds a new layer to the renderer. There must be a position given in order to place it on the right layer.
	 *
	 * @param pos   The position to add the layer.
	 * @param layer The layer to add.
	 */
	public void addLayer(int pos, Layer layer) {
		if (pos < 0 || pos >= MAX_LAYERS) {
			throw new IllegalArgumentException("Pos must be between 0 and " + (MAX_LAYERS - 1));
		}

		layers[pos] = layer;
		layersInitialized[pos] = false;
	}

	/**
	 * Removes the layer at position pos again.
	 *
	 * @param pos Removes a layer.
	 */
	public void removeLayer(int pos) {
		if (pos < 0 || pos >= MAX_LAYERS) {
			throw new IllegalArgumentException("Pos must be between 0 and " + (MAX_LAYERS - 1));
		}

		layers[pos] = null;
		layersInitialized[pos] = false;
	}

	@Override
	public void onRendererShutdown() {
		Log.i(TAG, "onRendererShutdown");
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		Log.i(TAG, "onSurfaceChanged");
	}

	/**
	 * Creates the buffers we use to store information about the 3D world.
	 * <p>
	 * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand. Hence we use
	 * ByteBuffers.
	 *
	 * @param config The EGL configuration used when creating the surface.
	 */
	@Override
	public void onSurfaceCreated(EGLConfig config) {
		Log.i(TAG, "onSurfaceCreated");
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f); // Dark background so text shows up well.
		GLHelper.checkGLError("onSurfaceCreated");

		// Position the wall in front of the user. (-1 unit/meter in worldspace).
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, 0f, 0f, Z_MODEL_POS);
		Matrix.setLookAtM(cameraMatrix, 0, 0.0f, 0.0f, CAMERA_Z, 0f, 0f, 0f, 0f, 1.0f, 0f);
	}

	/**
	 * Draws a frame for an eye.
	 *
	 * @param eye The eye to render. Includes all required transformations.
	 */
	@Override
	public void onDrawEye(Eye eye) {
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Remove the rotation from the matrix.
		float[] eyeMat = eye.getEyeView();
		eyeMat[0] = 1;
		eyeMat[1] = 0;
		eyeMat[2] = 0;
		eyeMat[4] = 0;
		eyeMat[5] = 1;
		eyeMat[6] = 0;
		eyeMat[8] = 0;
		eyeMat[9] = 0;
		eyeMat[10] = 1;

		// Now step through the different layer and render them each with a slightly z-offest towards the viewer
		// starting from index 0 as the farthest away.
		for (int i = 0; i < MAX_LAYERS; i++) {
			if (layers[i] == null) {
				continue;
			}

			// Set camera to eye position.
			Matrix.multiplyMM(modelViewProjection, 0, eyeMat, 0, cameraMatrix, 0);

			// Prepare the model matrix.
			Matrix.setIdentityM(modelMatrix, 0);

			final float modelZPos = -(Z_MODEL_POS - i * Z_LAYER_DISTANCE);
			Matrix.translateM(modelMatrix, 0, 0f, 0f, modelZPos);

			// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
			// (which currently contains model * view).
			Matrix.multiplyMM(modelViewProjection, 0, modelViewProjection, 0, modelMatrix, 0);

			// This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
			// (which now contains model * view * projection).
			Matrix.multiplyMM(modelViewProjection, 0, eye.getPerspective(Z_NEAR, Z_FAR), 0, modelViewProjection, 0);

			layers[i].onDrawEye(modelViewProjection);
		}
	}

	/**
	 * Calculate all needed steps which are required for all renderings of the scene.
	 *
	 * @param headTransform The head transformation in the new frame.
	 */
	@Override
	public void onNewFrame(HeadTransform headTransform) {
		// Check if new layers must be initialized, we must to this in the render thread.
		for (int i = 0; i < MAX_LAYERS; i++) {
			if (!layersInitialized[i] && layers[i] != null) {

				layers[i].onCreated();
				layersInitialized[i] = true;
			}

			if (layers[i] != null) {
				layers[i].onNewFrame(headTransform);
			}
		}

		// Tell the head gesture controller about the new position.
		if (gestureController != null) {
			gestureController.onHeadMovement(headTransform);
		}

		GLHelper.checkGLError("onNewFrame");
	}

	@Override
	public void onFinishFrame(Viewport viewport) {
		// no op.
	}
}
