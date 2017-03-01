package de.fraunhofer.ipa.vrread.graphics;

import android.content.Context;

import de.fraunhofer.ipa.vrread.graphics.shader.HelperLineShader;
import de.fraunhofer.ipa.vrread.graphics.shader.ScrollingTextureShader;

/**
 * Created by tbf on 24.02.2017.
 */

public class HelperLineLayer extends Layer {

	public HelperLineLayer(Context ctx) {
		super(new HelperLineShader(ctx));
	}

}
