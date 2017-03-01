package de.fraunhofer.ipa.vrread.graphics;

import android.content.Context;

import de.fraunhofer.ipa.vrread.graphics.shader.ScrollingTextureShader;

/**
 * Basic class for holding the textual representation. It has method to control the text. This is usually done by a
 * {@link de.fraunhofer.ipa.vrread.ReadController} which will give control commands to this layer.
 * <p>
 * Created by Thomas Felix on 24.02.2017.
 */

public class ScrollingTextLayer extends Layer {

	public ScrollingTextLayer(Context ctx) {
		super(new ScrollingTextureShader(ctx));
	}
}
