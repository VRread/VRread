package de.fraunhofer.ipa.vrread.graphics.layer;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.vr.sdk.base.HeadTransform;

import de.fraunhofer.ipa.vrread.control.ReadController;
import de.fraunhofer.ipa.vrread.graphics.shader.ScrollingTextureShader;

/**
 * Basic class for holding the textual representation. It has method to control the text. This is usually done by a
 * {@link ReadController} which will give control commands to this layer.
 * <p>
 * Created by Thomas Felix on 24.02.2017.
 */

public class ScrollingTextLayer extends Layer {

	private float x = 0;
	private float y = 0;
	private Bitmap newTexture;
	private final ScrollingTextureShader textShader;

	public ScrollingTextLayer(Context ctx) {
		super(new ScrollingTextureShader(ctx));

		// Get the shader back again.
		textShader = (ScrollingTextureShader) getShader();
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y % 1;
	}

	public void setX(float x) {
		this.x = x % 1;
	}

	/**
	 * Advises the layer to use a new bitmap. In the next render cycle the bitmap is exchanged.
	 * <p>
	 * Note: After the bitmap was used its memory is freed and its recycled. This is done in this method because it is
	 * not completly known when the render cycle picks up the saved texture.
	 *
	 * @param bitmap The new bitmap to use.
	 */
	public void setTexture(Bitmap bitmap) {
		this.newTexture = bitmap;
	}

	/**
	 * Transfer the new x and y uv coordinates into the shader.
	 *
	 * @param headTransform The new head tranform.
	 */
	@Override
	public void onNewFrame(HeadTransform headTransform) {
		super.onNewFrame(headTransform);

		// Transfer the uv coordiantes.
		textShader.setUv(x, y);
	}
}