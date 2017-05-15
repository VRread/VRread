package de.fraunhofer.ipa.vrread.graphics.layer;

import android.content.Context;

import de.fraunhofer.ipa.vrread.AppSettings;
import de.fraunhofer.ipa.vrread.graphics.shader.HelperLineShader;

/**
 * Draws a helper line by the use of a line shader onto the screen.
 * Created by tbf on 24.02.2017.
 */
public class HelperLineLayer extends Layer {

	private HelperLineShader shader;

	public HelperLineLayer(Context ctx) {
		super(new HelperLineShader(ctx));

		this.shader = (HelperLineShader) getShader();
	}

	/**
	 * Sets the position of the helper line. For the valid codes see {@link AppSettings#getHelperlinePosition()}.
	 *
	 * @param linePosition The line position on the screen.
	 */
	public void setLinePosition(int linePosition) {
		shader.setHelperlinePosition(linePosition);
	}
}
