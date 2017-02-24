package de.fraunhofer.ipa.vrread.graphics;

import android.content.Context;

/**
 * Basic class for holding the textual representation. It has method to control the text. This is usually done by a
 * {@link de.fraunhofer.ipa.vrread.ReadController} which will give control commands to this layer.
 * <p>
 * Created by Thomas Felix on 24.02.2017.
 */

public class ScrollingTextLayer extends Layer {

	private ScrollingTextureShader shader;

	public ScrollingTextLayer(Context ctx) {
		this.shader = new ScrollingTextureShader(ctx);
	}

	@Override
	void onCreated() {
		super.onCreated();

		shader.loadShader();
	}

	@Override
	void onDrawEye(float[] modelViewProjection) {
		shader.useShader();
		shader.setModelViewProjection(modelViewProjection);
		shader.setModelVertices(wallVertices);

		super.onDrawEye(modelViewProjection);
	}
}
