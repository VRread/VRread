package de.fraunhofer.ipa.vrread.control;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Objects;

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

	private Datasource datasource;
	private final ScrollingTextLayer textLayer;
	/**
	 * Distance to be scrolled when a looking method is called.
	 */
	private float scrollDistanceIncrement = 0.04f;

	private float scale = 1f;
	private ReadPosition readPosition;

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
		createTexture();
	}

	public void down() {
		float y = readPosition.getY() + scrollDistanceIncrement / scale;
		if (y > 1) {
			y = 1;
		}
		readPosition.setY(y);
		createTexture();
	}

	public void left() {

		float currentLayerPos = textLayer.getX();
		currentLayerPos -= scrollDistanceIncrement / scale;

		if(currentLayerPos < 0.5) {
			textLayer.setX(currentLayerPos);
		} else {
			float x = readPosition.getX() - scrollDistanceIncrement / scale;
			if (x < 0) {
				x = 0;
			}
			//readPosition.setX(x);
			//createTexture();
		}
	}

	public void right() {

		float currentLayerPos = textLayer.getX();
		currentLayerPos += scrollDistanceIncrement / scale;

		if(currentLayerPos < 0.5) {
			textLayer.setX(currentLayerPos);
		} else {
			// We need to increment (jump) on read position and render new layer.
			float x = readPosition.getX() + scrollDistanceIncrement / scale;
			if (x > 1) {
				x = 1;
			}
			//readPosition.setX(x);
			//createTexture();
		}
	}

	private void createTexture() {
		Log.d(TAG, String.format("Creating texture position: %s", readPosition));

		final TextureSize texSize = textLayer.getTextureSize();
		final Bitmap bitmap = datasource.getTextureBitmap(readPosition, scale, texSize);
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

			createTexture();
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

			createTexture();
		}
	}

	/**
	 * Go to a specific page.
	 *
	 * @param page The page to jump to.
	 */
	public void gotoPage(int page) {
		if(page >= 0 && page < datasource.getPageCount()) {
			readPosition.setPage(page);
			createTexture();
		}
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
}
