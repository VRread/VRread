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
	private final static int NUM_CALLS_PAGE_CHANGE = 10;
	private final ScrollingTextLayer textLayer;
	private Datasource datasource;
	/**
	 * Distance to be scrolled when a looking method is called.
	 */
	private float baseVelocitySec = 200f;
	private long lastRenderTime = 0;
	private float scale = 1f;
	private ReadPosition readPosition;
	private ReadPosition tempReadPosition = new ReadPosition();
	private ReadPosition lastRenderPosition = new ReadPosition();
	private float distance;
	private int nextPageDelayCounter = 0;
	private int scrollSpeedFactor = 1;

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
		float delay = now - lastRenderTime;
		lastRenderTime = now;

		if (delay > 100) {
			lastRenderTime = now;
			return false;
		}

		distance = baseVelocitySec * scrollSpeedFactor * delay / 1000f;
		return true;
	}

	public void up() {
		if (!shouldStepFrame()) {
			return;
		}

		readPosition.setY(readPosition.getY() - distance);
		float texDistance = textLayer.getY() - distance / textLayer.getTextureSize().getHeight();

		if (texDistance > 0) {
			textLayer.setY(texDistance);
		} else {
			// Start of page reached do nothing.
			if (readPosition.getY() <= 0.01) {
				return;
			}

			// Reached and of tex. Render new.
			//1024 * 0.5
			ReadPosition newPos = new ReadPosition(readPosition.getPage(), lastRenderPosition.getX(), readPosition
					.getY() - 512);
			createTexture(newPos);
			textLayer.setY(0.5f);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f", readPosition.getX(),
				readPosition.getY(), textLayer.getX(), textLayer.getY()));
	}

	public void down() {
		if (!shouldStepFrame()) {
			return;
		}

		float newY = readPosition.getY() + distance;
		float texDistance = textLayer.getY() + distance / textLayer.getTextureSize().getHeight();

		tempReadPosition.set(readPosition);
		tempReadPosition.setX(newY);

		if (!datasource.isInsidePage(tempReadPosition, scale)) {
			nextPageDelayCounter++;
			if (nextPageDelayCounter > NUM_CALLS_PAGE_CHANGE) {
				nextPage();
				nextPageDelayCounter = 0;
			}
			return;
		}

		readPosition.setY(newY);

		if (texDistance < 0.5) {
			textLayer.setY(texDistance);
		} else {
			// Reached and of tex. Render new.
			ReadPosition newPos = new ReadPosition(readPosition.getPage(), lastRenderPosition.getX(), readPosition
					.getY());
			createTexture(newPos);
			textLayer.setY(0);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f", readPosition.getX(),
				readPosition.getY(), textLayer.getX(), textLayer.getY()));
	}

	public void left() {
		if (!shouldStepFrame()) {
			return;
		}

		readPosition.setX(readPosition.getX() - distance);
		float texDistance = textLayer.getX() - distance / 1024;

		if (texDistance > 0) {
			textLayer.setX(texDistance);
		} else {

			// Start of page reached do nothing.
			if (readPosition.getX() <= 0.01) {
				return;
			}

			// Reached and of tex. Render new.
			ReadPosition newPos = new ReadPosition(readPosition.getPage(), readPosition.getX() - 512,
					lastRenderPosition.getY());
			createTexture(newPos);
			textLayer.setX(0.5f);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f", readPosition.getX(),
				readPosition.getY(), textLayer.getX(), textLayer.getY()));
	}

	public void right() {
		if (!shouldStepFrame()) {
			return;
		}

		float newX = readPosition.getX() + distance;
		float texDistance = textLayer.getX() + distance / 1024;

		tempReadPosition.set(readPosition);
		tempReadPosition.setX(newX);

		if (!datasource.isInsidePage(tempReadPosition, scale)) {
			return;
		}

		readPosition.setX(newX);

		if (texDistance < 0.5) {
			textLayer.setX(texDistance);
		} else {
			// Reached and of tex. Render new.
			ReadPosition newPos = new ReadPosition(readPosition.getPage(), readPosition.getX(), lastRenderPosition
					.getY());
			createTexture(newPos);
			textLayer.setX(0);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f", readPosition.getX(),
				readPosition.getY(), textLayer.getX(), textLayer.getY()));
	}

	private void createTexture(ReadPosition texturePosition) {
		Log.d(TAG, String.format("Creating texture position: %s", texturePosition));

		lastRenderPosition.set(texturePosition);

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
			textLayer.setX(0);
			textLayer.setY(0);

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
			readPosition.setY(0);
			readPosition.setX(0);
			textLayer.setX(0);
			textLayer.setY(0);
			createTexture(readPosition);
		}
	}

	/**
	 * Sets the zoom scale factor of the document size.
	 *
	 * @param scale The new zoom factor.
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * Sets the speed with which the document is scrolled.
	 *
	 * @param scrollSpeedFactor The new scrollspeed.
	 */
	public void setScrollSpeedFactor(int scrollSpeedFactor) {
		this.scrollSpeedFactor = scrollSpeedFactor;
	}
}
