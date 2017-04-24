package de.fraunhofer.ipa.vrread.control;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Objects;
import java.util.Stack;

import de.fraunhofer.ipa.vrread.datasource.Datasource;
import de.fraunhofer.ipa.vrread.datasource.ReadPosition;
import de.fraunhofer.ipa.vrread.datasource.TextureSize;
import de.fraunhofer.ipa.vrread.graphics.layer.ScrollingTextLayer;

/**
 * The read controller is holding the current reading position and reacts upon control requests. It will calculate a new
 * reading position, see if it can just correct the visual appearance at the shader or if it needs to send a whole new
 * texture to the the rendering shader.
 * <p>
 * Created by Thomas Felix on 23.02.2017.
 */

public class ReadController {

	private final static String TAG = ReadController.class.getSimpleName();
	private final ScrollingTextLayer textLayer;
	private Datasource datasource;
	/**
	 * Distance to be scrolled when a looking method is called.
	 */
	private float scrollDistanceIncrement = 0.006f;
	private float scale = 1f;
	private ReadPosition readPosition;

	/**
	 * Holds the distance onf the page until a new texture tile is rendered. This is important for stepping back.
	 * This increment is set when the first step occures.
	 */
	private float xIncrement = Float.NaN;

	/**
	 * @param textLayer The textlayer to work upon when receiving the movement commands.
	 */
	ReadController(ScrollingTextLayer textLayer) {

		this.textLayer = Objects.requireNonNull(textLayer);
		this.readPosition = new ReadPosition(0, 0, 0);
	}

	/**
	 * Sets the datasource to a new one. This will usually reset the current reading position.
	 *
	 * @param datasource The new datasource which will be queried for rendered textures.
	 */
	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	public void up() {
		float y = readPosition.getY() - scrollDistanceIncrement / scale;
		if (y < 0) {
			y = 0;
		}
		readPosition.setY(y);
		createTexture(readPosition);
	}

	public void down() {
		// We need to increment (jump) on read position and render new layer.
		float currentTextPos = readPosition.getY();
		currentTextPos += scrollDistanceIncrement / scale;

		if (currentTextPos <= 1.5) {
			readPosition.setY(currentTextPos);
		}

		// Only increment the texture position if there is room left.
		float currentLayerPos = textLayer.getY();
		currentLayerPos += scrollDistanceIncrement / scale;
		if (currentLayerPos < 0.5) {

			textLayer.setY(currentLayerPos);

		} else {
			// If there is no room left, create a new texture.

			// Save the step up increment if needed for going back.
			if (Float.isNaN(xIncrement)) {
				xIncrement = readPosition.getX();
			}

			if (currentTextPos < 1) {
				//readPosition.setX(0.628f);
				createTexture(readPosition);
				textLayer.setY(0);
			}
		}
	}

	public void left() {

		// We need to increment (jump) on read position and render new layer.
		float currentTextPos = readPosition.getX();
		currentTextPos -= scrollDistanceIncrement / scale;

		if (currentTextPos > 0) {
			readPosition.setX(currentTextPos);
		}
		//readPosition.setX(currentTextPos);

		// Only increment the texture position if there is room left.

		float currentLayerPos = textLayer.getX();
		currentLayerPos -= scrollDistanceIncrement / scale;

		if (currentLayerPos > 0) {
			textLayer.setX(currentLayerPos);

		} else {
			// If there is no room left, create a new texture.
			if (currentTextPos > 0) {

				//readPosition.setX(lastXPos);
				ReadPosition nextTexPos = new ReadPosition(readPosition.getPage(),
						readPosition.getX() - xIncrement,
						readPosition.getY());
				createTexture(nextTexPos);
				textLayer.setX(0.5f);
			}
		}
	}

	public void right() {

		// We need to increment (jump) on read position and render new layer.
		float currentTextPos = readPosition.getX();
		currentTextPos += scrollDistanceIncrement / scale;

		if (currentTextPos <= 1.5) {
			readPosition.setX(currentTextPos);
		}

		// Only increment the texture position if there is room left.
		float currentLayerPos = textLayer.getX();
		currentLayerPos += scrollDistanceIncrement / scale;
		if (currentLayerPos < 0.5) {

			textLayer.setX(currentLayerPos);

		} else {
			// If there is no room left, create a new texture.

			// Save the step up increment if needed for going back.
			if (Float.isNaN(xIncrement)) {
				xIncrement = readPosition.getX();
			}

			if (currentTextPos < 1) {
				//readPosition.setX(0.628f);
				createTexture(readPosition);
				textLayer.setX(0);
			}
		}
	}

	private void createTexture(ReadPosition texturePosition) {
		Log.d(TAG, String.format("Creating texture position: %s", texturePosition));

		final TextureSize texSize = textLayer.getTextureSize();
		final Bitmap bitmap = datasource.getTextureBitmap(texturePosition, scale, texSize);
		textLayer.setTexture(bitmap);
	}

	public ReadPosition getReadPosition() {
		return readPosition;
	}

	/**
	 * Manually switch to the next page.
	 */
	public void nextPage() {
		if (readPosition.getPage() + 1 < datasource.getPageCount()) {
			readPosition.setPage(readPosition.getPage() + 1);
			// Set to top left position.
			readPosition.setY(0);
			readPosition.setX(0);

			createTexture(readPosition);
		}
	}

	/**
	 * Manually switch to the previous page.
	 */
	public void previousPage() {
		if (readPosition.getPage() - 1 >= 0) {
			readPosition.setPage(readPosition.getPage() - 1);
			// Set back to middle bottom position.
			readPosition.setY(1);
			readPosition.setX(0.5f);

			createTexture(readPosition);
		}
	}

	/**
	 * Go to a specific page.
	 *
	 * @param page The page to jump to.
	 */
	public void gotoPage(int page) {
		if (page >= 0 && page < datasource.getPageCount()) {
			readPosition.setPage(page);
			createTexture(readPosition);
		}
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
}
