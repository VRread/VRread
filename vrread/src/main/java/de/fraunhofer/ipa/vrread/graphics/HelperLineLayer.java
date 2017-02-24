package de.fraunhofer.ipa.vrread.graphics;

import android.content.Context;

import de.fraunhofer.ipa.vrread.graphics.shader.ScrollingTextureShader;

/**
 * Created by tbf on 24.02.2017.
 */

public class HelperLineLayer extends Layer {

	private ScrollingTextureShader shader;

	public HelperLineLayer(Context ctx) {
		//this.shader = new ScrollingTextureShader(ctx);
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
