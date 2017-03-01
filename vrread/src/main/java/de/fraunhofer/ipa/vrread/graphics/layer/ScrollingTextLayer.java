package de.fraunhofer.ipa.vrread.graphics.layer;

import android.content.Context;
import android.graphics.Bitmap;

import de.fraunhofer.ipa.vrread.control.ReadController;
import de.fraunhofer.ipa.vrread.graphics.shader.ScrollingTextureShader;

/**
 * Basic class for holding the textual representation. It has method to control the text. This is usually done by a
 * {@link ReadController} which will give control commands to this layer.
 * <p>
 * Created by Thomas Felix on 24.02.2017.
 */

public class ScrollingTextLayer extends Layer {

	private int x = 0;
	private int y = 0;
	private Bitmap newTexture;

	public ScrollingTextLayer(Context ctx) {
		super(new ScrollingTextureShader(ctx));
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
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
}
