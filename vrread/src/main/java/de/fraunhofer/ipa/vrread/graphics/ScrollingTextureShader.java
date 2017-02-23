package de.fraunhofer.ipa.vrread.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.fraunhofer.ipa.vrread.R;

/**
 * This creates a shader which is able to draw, scroll and scale a texture.
 * <p>
 * Created by Thomas Felix on 23.02.2017.
 */

public class ScrollingTextureShader extends Shader {

	private final Context ctx;

	private FloatBuffer wallTextureCoordinates;

	private int textureUvOffsetParam;
	private int textureScaleParam;

	private int wallProgram;
	private int wallPositionParam;
	private int wallModelViewProjectionParam;

	// This will be used to pass in the texture image.
	private int textureColorParam;

	// This will be used to pass in model texture coordinate information.
	private int textureCoordinateParam;

	// This is a handle to our texture data.
	private int textureDataHandle;


	/**
	 * The current scale of the texture. This parameter is fed into the shader in each rendering step.
	 */
	private float textureScale = 1.0f;

	/**
	 * Offset uv coordinates which are fed in each rendering to the shader in order to shift  the  texture.
	 */
	private float[] textureUvOffset = new float[]{0f, 0f};

	public synchronized void setTextureScale(float textureScale) {
		this.textureScale = textureScale;
	}

	private synchronized float getTextureScale() {
		return textureScale;
	}

	/**
	 * Converts a raw text file into a string.
	 *
	 * @param resId The resource ID of the raw text file about to be turned into a shader.
	 * @return The context of the text file, or null in case of error.
	 */
	private String readRawTextFile(int resId) {
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

	public int loadTexture(final int resourceId) {

		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inScaled = false;   // No pre-scaling

			// Read in the resource
			final Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), resourceId, options);

			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering of the texture
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

			// Recycle the bitmap, since its data has been loaded into OpenGL.
			bitmap.recycle();
		}

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	ScrollingTextureShader(Context ctx) {

		this.ctx = ctx;
	}

	@Override
	public void useShader() {
		GLES20.glUseProgram(wallProgram);

		// ### Prepare Texture
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);
		GLES20.glUniform1i(textureColorParam, 0);
		GLHelper.checkGLError("texture binding");

		// Set the texture uv coordinates.
		GLES20.glVertexAttribPointer(textureCoordinateParam, 2, GLES20.GL_FLOAT, false, 0, wallTextureCoordinates);
		GLES20.glEnableVertexAttribArray(textureCoordinateParam);

		// Send scale and offset
		GLES20.glUniform1f(textureScaleParam, getTextureScale());
		GLES20.glUniform2f(textureUvOffsetParam, textureUvOffset[0], textureUvOffset[1]);
		GLHelper.checkGLError("texture scale offset");
	}

	@Override
	public void loadShader() {
		// Get the UV coordiantes.
		ByteBuffer bbFloorTexCords = ByteBuffer.allocateDirect(WorldLayoutData.PLANE_TEX_CORDS.length * 4);
		bbFloorTexCords.order(ByteOrder.nativeOrder());
		wallTextureCoordinates = bbFloorTexCords.asFloatBuffer();
		wallTextureCoordinates.put(WorldLayoutData.PLANE_TEX_CORDS);
		wallTextureCoordinates.position(0);

		final int textVertShader = loadGLShader(readRawTextFile(R.raw.vertex), GLES20.GL_VERTEX_SHADER);
		final int textFragShader = loadGLShader(readRawTextFile(R.raw.fragment), GLES20.GL_FRAGMENT_SHADER);

		wallProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(wallProgram, textVertShader);
		GLES20.glAttachShader(wallProgram, textFragShader);
		GLES20.glLinkProgram(wallProgram);

		// Error check the linkage.
		// Get the link status.
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(wallProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

		// If the link failed, delete the program.
		if (linkStatus[0] == 0) {
			GLES20.glDeleteProgram(wallProgram);
			wallProgram = 0;
		}
		if (wallProgram == 0) {
			throw new RuntimeException("Error creating program.");
		}

		GLHelper.checkGLError("Floor program");

		// Load texture
		textureDataHandle = loadTexture(R.drawable.text);

		GLES20.glUseProgram(wallProgram);
		wallPositionParam = GLES20.glGetAttribLocation(wallProgram, "a_Position");
		wallModelViewProjectionParam = GLES20.glGetUniformLocation(wallProgram, "u_MVPMatrix");

		// Texture params
		textureCoordinateParam = GLES20.glGetAttribLocation(wallProgram, "a_TexCoordinate");
		textureColorParam = GLES20.glGetUniformLocation(wallProgram, "u_Texture");
		textureScaleParam = GLES20.glGetUniformLocation(wallProgram, "u_Scale");
		textureUvOffsetParam = GLES20.glGetUniformLocation(wallProgram, "u_Offset");

		GLHelper.checkGLError("Floor program params");
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

	public int useTexture(Bitmap bitmap) {
		final int[] textureHandle = new int[1];

		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] != 0) {
			// Bind to the texture in OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

			// Set filtering of the texture
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

			// Load the bitmap into the bound texture.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		}

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}

		return textureHandle[0];
	}

	/**
	 * Replaces the text texture.
	 */
	public void replaceTexture(int oldTextureHandle) {
		//textureDataHandle = loadTexture(R.drawable.text_inv);

		final int[] textureHandle = new int[]{oldTextureHandle};
		GLES20.glDeleteTextures(1, textureHandle, 0);
	}
}
