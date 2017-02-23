package de.fraunhofer.ipa.vrread.datasource;

import android.graphics.Bitmap;

import de.fraunhofer.ipa.vrread.ReadPosition;
import de.fraunhofer.ipa.vrread.TextureSize;

/**
 * This refers to a datasource. It will provide a method for rendering new texture bitmaps to be used by the renderer.
 * <p>
 * Created by Thomas Felix on 22.02.2017.
 */
public abstract class Datasource {

	/**
	 * Returns a rendered bitmap for the given
	 *
	 * @param position The position of the user currently reading.
	 * @param scale    The current scale level of the texture. 1.0 is normal scale. Bigger then 1 makes the rendered
	 *                 texture bigger. Smaller values smaller. Must be bigger then 0.
	 * @param size     The texture size in px. Usually they are multiplies of 2.
	 * @return Returns a bitmap for rendering.
	 */
	public abstract Bitmap getTextureBitmap(ReadPosition position, float scale, TextureSize size);
}
