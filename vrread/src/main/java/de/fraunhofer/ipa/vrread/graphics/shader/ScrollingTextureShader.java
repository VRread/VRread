package de.fraunhofer.ipa.vrread.graphics.shader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.control.Contrast;

/**
 * This creates a shader which is able to draw, scroll and scale a texture.
 * <p>
 * Created by Thomas Felix on 23.02.2017.
 */

public class ScrollingTextureShader extends QuadShader {

	private Context ctx;

	private int textureUvOffsetParam;
	private int textureScaleParam;
	private int contrastModeParam;

	// This is a handle to our texture data.
	private int textureDataHandle = -1;


	/**
	 * The current scale of the texture. This parameter is fed into the shader in each rendering step.
	 */
	private float textureScale = 1.0f;

	/**
	 * Offset uv coordinates which are fed in each rendering to the shader in order to shift  the  texture.
	 */
	private float u = 0f;
	private float v = 0f;

	private Contrast contrastMode = Contrast.NORMAL;


	public synchronized void setTextureScale(float textureScale) {
		// Invert this because the uv scale up works with the inverse.
		this.textureScale = textureScale;
	}

	public synchronized void setUv(float u, float v) {
		this.u = u;
		this.v = v;
	}

	public synchronized  void setContrastMode(Contrast contrast) {
		this.contrastMode = contrast;
	}

	private synchronized float getTextureScale() {
		return textureScale;
	}

	public ScrollingTextureShader(Context ctx) {

		this.ctx = ctx;
	}

	@Override
	public void useShader() {
		GLES20.glUseProgram(quadProgram);

		// ### Prepare Texture
		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);
		GLHelper.checkGLError("texture binding");

		// Set the texture uv coordinates.
		GLES20.glVertexAttribPointer(textureCoordinateParam, 2, GLES20.GL_FLOAT, false, 0, wallTextureCoordinates);
		GLES20.glEnableVertexAttribArray(textureCoordinateParam);

		// Send scale and offset
		GLES20.glUniform1f(textureScaleParam, getTextureScale());
		GLES20.glUniform2f(textureUvOffsetParam, u, v);
		GLES20.glUniform1i(contrastModeParam, contrastMode.ordinal());
		GLHelper.checkGLError("texture scale offset");
	}

	@Override
	protected void createShaderProgram() {
		final int textVertShader = loadGLShader(GLHelper.readRawTextFile(ctx, R.raw.vertex), GLES20.GL_VERTEX_SHADER);
		final int textFragShader = loadGLShader(GLHelper.readRawTextFile(ctx, R.raw.fragment),
				GLES20.GL_FRAGMENT_SHADER);

		quadProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(quadProgram, textVertShader);
		GLES20.glAttachShader(quadProgram, textFragShader);
		GLES20.glLinkProgram(quadProgram);
	}

	@Override
	public void loadShader() {
		super.loadShader();

		// Load texture
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;   // No pre-scaling
		final Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.text, options);
		useTexture(bitmap);

		GLES20.glUseProgram(quadProgram);

		// Texture params
		textureScaleParam = GLES20.glGetUniformLocation(quadProgram, "u_Scale");
		textureUvOffsetParam = GLES20.glGetUniformLocation(quadProgram, "u_Offset");
		contrastModeParam = GLES20.glGetUniformLocation(quadProgram, "u_ContrastMode");

		GLHelper.checkGLError("loadShader params");
	}

	public void useTexture(Bitmap bitmap) {

		if (textureDataHandle != -1) {
			// Replace/delete the old texture.
			final int[] textureHandle = new int[]{textureDataHandle};
			GLES20.glDeleteTextures(1, textureHandle, 0);
		}

		final int[] textureHandle = new int[1];
		GLES20.glGenTextures(1, textureHandle, 0);

		if (textureHandle[0] == 0) {
			throw new RuntimeException("Error loading texture.");
		}

		// Bind to the texture in OpenGL
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

		// Set filtering of the texture
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		textureDataHandle = textureHandle[0];
	}
}
