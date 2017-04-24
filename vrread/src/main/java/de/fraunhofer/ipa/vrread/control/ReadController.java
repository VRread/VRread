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
	private float baseVelocitySec = 100f;
	private long lastRender = 0;
	private float scale = 1f;
	private ReadPosition readPosition;
	private float delay;
	private float distance;

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

	private boolean shouldStepFrame() {
		long now = System.currentTimeMillis();
		delay = now - lastRender;
		lastRender = now;

		if(delay > 100) {
			lastRender = now;
			return false;
		}

		distance = baseVelocitySec * delay / 1000f;
		return true;
	}

	public void up() {

	}

	public void down() {
		if(!shouldStepFrame()) {
			return;
		}

		readPosition.setY(readPosition.getY() + distance);
		float texDistance = textLayer.getY() + distance / textLayer.getTextureSize().getHeight();

		Log.d(TAG, String.format("Tex Pos Y: %f, Read Pos Y: %f", readPosition.getY(), texDistance));

		if(texDistance < 0.5) {
			textLayer.setY(texDistance);
		} else {
			// Reached and of tex. Render new.
			createTexture(readPosition);
			textLayer.setY(0);
		}
	}

	public void left() {
		if(!shouldStepFrame()) {
			return;
		}

		readPosition.setX(readPosition.getX() - distance);
		float texDistance = textLayer.getX() - distance / 1024;

		Log.d(TAG, String.format("Tex Pos X: %f, Read Pos X: %f", readPosition.getX(), texDistance));

		if(texDistance > 0) {
			textLayer.setX(texDistance);
		} else {

			// Start of page reached do nothing.
			if(readPosition.getX() <= 0.01) {
				return;
			}

			// Reached and of tex. Render new.
			//1024 * 0.5
			readPosition.setX(readPosition.getX() - 512);
			createTexture(readPosition);
			textLayer.setX(0.5f);
		}

	}

	public void right() {

		long now = System.currentTimeMillis();
		long delay = now - lastRender;

		if(delay > 100) {
			lastRender = now;
			return;
		}

		lastRender = now;
		float distance = baseVelocitySec * delay / 1000f;

		readPosition.setX(readPosition.getX() + distance);
		float texDistance = textLayer.getX() + distance / 1024;

		Log.d(TAG, String.format("Tex Pos X: %f, Read Pos X: %f", readPosition.getX(), texDistance));

		if(texDistance < 0.5) {
			textLayer.setX(texDistance);
		} else {
			// Reached and of tex. Render new.
			createTexture(readPosition);
			textLayer.setX(0);
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
