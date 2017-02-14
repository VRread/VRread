/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fraunhofer.ipa.vrread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * A Google VR sample application. </p><p> The TreasureHunt scene consists of a planar ground grid and a floating
 * "treasure" cube. When the user looks at the cube, the cube will turn gold. While gold, the user can activate the
 * Cardboard trigger, which will in turn randomly reposition the cube.
 */
public class VRViewActivity extends GvrActivity implements GvrView.StereoRenderer {

	private static final String TAG = VRViewActivity.class.getSimpleName();

	private static final float Z_NEAR = 0.1f;
	private static final float Z_FAR = 10.0f;

	private static final float CAMERA_Z = 0.5f;

	private FloatBuffer wallVertices;

	private int wallProgram;
	private int wallPositionParam;
	private int wallModelViewProjectionParam;

	/**
	 * Position of the camera.
	 */
	private float[] cameraMatrix;


	/**
	 * All the cam transforms are feeded into this matrix.
	 */
	private float[] viewMatrix;

	/**
	 * Positions the model.
	 */
	private float[] modelWall;

	/**
	 * Holds the rotation of the head.
	 */
	private float[] headQuaternion = new float[4];

	/**
	 * Projection matrix.
	 */
	private float[] modelViewProjection;

	// Store our model data in a float buffer.
	private FloatBuffer wallTextureCoordinates;

	// This will be used to pass in the texture image.
	private int textureColorParam;

	// This will be used to pass in model texture coordinate information.
	private int textureCoordinateParam;

	private int textureUvOffsetParam;

	private int textureScaleParam;

	// This is a handle to our texture data.
	private int textureDataHandle;

	/**
	 * The current scale of the texture. This parameter is fed into the shader in each
	 * rendering step.
	 */
	private float textureScale = 1.0f;

	/**
	 * Offset uv coordinates which are fed in each rendering to the shader in order to
	 * shift  the  texture.
	 */
	private float[] textureUvOffset = new float[] {0f, 0f};

	private Vibrator vibrator;

	private GvrView gvrView;


	/**
	 * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
	 *
	 * @param type  The type of shader we will be creating.
	 * @param resId The resource ID of the raw text file about to be turned into a shader.
	 * @return The shader object handler.
	 */
	private int loadGLShader(int type, int resId) {
		String code = readRawTextFile(resId);
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, code);
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

	/**
	 * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
	 *
	 * @param label Label to report in case of error.
	 */
	private static void checkGLError(String label) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, label + ": glError " + error);
			throw new RuntimeException(label + ": glError " + error);
		}
	}

	/**
	 * Sets the viewMatrix to our GvrView and initializes the transformation matrices we will use to render our scene.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initializeGvrView();

		cameraMatrix = new float[16];
		modelViewProjection = new float[16];
		modelWall = new float[16];
		viewMatrix = new float[16];
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	public void initializeGvrView() {
		setContentView(R.layout.common_ui);

		gvrView = (GvrView) findViewById(R.id.gvr_view);
		gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

		gvrView.setRenderer(this);
		gvrView.setTransitionViewEnabled(true);

		if (gvrView.setAsyncReprojectionEnabled(true)) {
			// Async reprojection decouples the app framerate from the display framerate,
			// allowing immersive interaction even at the throttled clockrates set by
			// sustained performance mode.
			AndroidCompat.setSustainedPerformanceMode(this, true);
		}

		setGvrView(gvrView);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
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

		// make a floor
		ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
		bbFloorVertices.order(ByteOrder.nativeOrder());
		wallVertices = bbFloorVertices.asFloatBuffer();
		wallVertices.put(WorldLayoutData.FLOOR_COORDS);
		wallVertices.position(0);

		ByteBuffer bbFloorTexCords = ByteBuffer.allocateDirect(WorldLayoutData.PLANE_TEX_CORDS.length * 4);
		bbFloorTexCords.order(ByteOrder.nativeOrder());
		wallTextureCoordinates = bbFloorTexCords.asFloatBuffer();
		wallTextureCoordinates.put(WorldLayoutData.PLANE_TEX_CORDS);
		wallTextureCoordinates.position(0);

		int textVertShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
		int textFragShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment);

		wallProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(wallProgram, textVertShader);
		GLES20.glAttachShader(wallProgram, textFragShader);

		GLES20.glLinkProgram(wallProgram);

		// Error check the linkage.
		// Get the link status.
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(wallProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

		// If the link failed, delete the program.
		if (linkStatus[0] == 0)
		{
			GLES20.glDeleteProgram(wallProgram);
			wallProgram = 0;
		}
		if (wallProgram == 0)
		{
			throw new RuntimeException("Error creating program.");
		}

		checkGLError("Floor program");

		// Load texture
		textureDataHandle = loadTexture(this, R.drawable.text);

		GLES20.glUseProgram(wallProgram);
		wallPositionParam = GLES20.glGetAttribLocation(wallProgram, "a_Position");
		wallModelViewProjectionParam = GLES20.glGetUniformLocation(wallProgram, "u_MVPMatrix");

		// Texture params
		textureCoordinateParam = GLES20.glGetAttribLocation(wallProgram, "a_TexCoordinate");
		textureColorParam = GLES20.glGetUniformLocation(wallProgram, "u_Texture");
		textureScaleParam = GLES20.glGetUniformLocation(wallProgram, "u_Scale");
		textureUvOffsetParam = GLES20.glGetUniformLocation(wallProgram, "u_Offset");

		checkGLError("Floor program params");

		// Position the wall in front of the user.
		Matrix.setIdentityM(modelWall, 0);
		Matrix.translateM(modelWall, 0, 0f, 0f, -1.0f);

		checkGLError("onSurfaceCreated");
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
		GLES20.glUseProgram(wallProgram);

		checkGLError("basic setup");

		// Apply the eye transformation to the camera.
		Matrix.multiplyMM(viewMatrix, 0, eye.getEyeView(), 0, cameraMatrix, 0);

		// Build the ModelView and ModelViewProjection matrices
		// for calculating cube position and light.
		float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(modelViewProjection, 0, viewMatrix, 0, modelWall, 0);

		// This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelViewProjection, 0);

		// ### Prepare Texture
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);
		GLES20.glUniform1i(textureColorParam, 0);
		checkGLError("texture binding");

		// Send the texture coordiantes, scale and position.
		GLES20.glVertexAttribPointer(textureCoordinateParam, 2, GLES20.GL_FLOAT, false, 0, wallTextureCoordinates);
		GLES20.glEnableVertexAttribArray(textureCoordinateParam);
		GLES20.glUniform1f(textureScaleParam, textureScale);
		GLES20.glUniform2f(textureUvOffsetParam, textureUvOffset[0], textureUvOffset[1]);
		checkGLError("texture coordiantes");

		// Set the position of the floor
		GLES20.glVertexAttribPointer(wallPositionParam, 3, GLES20.GL_FLOAT, false, 0, wallVertices);
		GLES20.glUniformMatrix4fv(wallModelViewProjectionParam, 1, false, modelViewProjection, 0);
		// Set the normal positions of the cube, again for shading
		GLES20.glEnableVertexAttribArray(wallPositionParam);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		checkGLError("drawing floor");
	}

	/**
	 * Converts a raw text file into a string.
	 *
	 * @param resId The resource ID of the raw text file about to be turned into a shader.
	 * @return The context of the text file, or null in case of error.
	 */
	private String readRawTextFile(int resId) {
		InputStream inputStream = getResources().openRawResource(resId);
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

	/**
	 * Calculate all needed steps which are required for all renderings of the scene.
	 *
	 * @param headTransform The head transformation in the new frame.
	 */
	@Override
	public void onNewFrame(HeadTransform headTransform) {

		// Build the camera matrix and apply it to the ModelView.
		Matrix.setLookAtM(cameraMatrix, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0f, 0.0f, 1.0f, 0.0f);

		// Get deviation of the head angle from the z direction with regards to the y axis.
		headTransform.getQuaternion(headQuaternion, 0);

		checkGLError("onReadyToDraw");
	}

	@Override
	public void onFinishFrame(Viewport viewport) {
	}


	/**
	 * Called when the Cardboard trigger is pulled.
	 */
	@Override
	public void onCardboardTrigger() {
		Log.i(TAG, "onCardboardTrigger");

		// Always give user feedback.
		vibrator.vibrate(50);
	}

	public int loadTexture(final Context context, final int resourceId)
	{
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0)
		{
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;   // No pre-scaling

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering of the texture
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			// http://stackoverflow.com/questions/9863969/updating-a-texture-in-opengl-with-glteximage2d
			//GLUtils.texS

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}

		if (textureHandle[0] == 0)
		{
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	public void onClick(View view) {
		//gvrView.recenterHeadTracker();

		//textureScale /= 1.1;
		textureUvOffset[0] += 0.5;
		textureUvOffset[1] += 0.7;
	}
}
