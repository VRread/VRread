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

	// Set this value to modify the rendered FPS.
	private final static int TARGET_FPS = 50;

	private final static int RENDER_DELAY_MS = 1000 / TARGET_FPS;

	// Base speed of the application.
	private final static float BASE_VELOCITY = 100f;

	// We sometimes need this size in order to calculate read distances.
	private final int textureSize;

	private final ScrollingTextLayer textLayer;
	private Datasource datasource;

	private long lastRenderTime = System.currentTimeMillis();

	private float renderDelay = 0f;
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

		if(textLayer.getTextureSize().getHeight() != textLayer.getTextureSize().getWidth()) {
			throw new IllegalArgumentException("Currently only qudratic texture sizes are supported.");
		}

		textureSize = textLayer.getTextureSize().getHeight();
	}

	/**
	 * Sets the datasource to a new one. This will usually reset the current reading position.
	 *
	 * @param datasource The new datasource which will be queried for rendered textures.
	 */
	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	/**
	 * Calculates if the app should perform a render step. Which should create 25 fps.
	 *
	 * @return TRUE if a new frame should be rendered. FALSE if not.
	 */
	private boolean shouldRenderFrame() {

		final long now = System.currentTimeMillis();

		if (now - lastRenderTime < RENDER_DELAY_MS) {
			return false;
		}

		renderDelay = now - lastRenderTime;

		return true;
	}

	/**
	 * Calculates the distance which the document has to be moved since the last render step.
	 *
	 * @param externalSpeedFactor The external speed factor.
	 */
	private void calculateMovedDistance(float externalSpeedFactor) {
		// Calculate the distance which was moved since the last frame.
		// (scale / 17)
		distance = BASE_VELOCITY * scrollSpeedFactor * externalSpeedFactor * renderDelay / 1000f;
	}

	void up(float speedFactor) {

		if (!shouldRenderFrame()) {
			return;
		}

		calculateMovedDistance(speedFactor);

		lastRenderTime = System.currentTimeMillis();

		float newY = readPosition.getY() - distance;
		float texDistance = textLayer.getY() - distance / textureSize;

		tempReadPosition.set(readPosition);
		tempReadPosition.setY(newY);

		if (!datasource.isInsidePage(tempReadPosition, scale)) {
			nextPageDelayCounter++;
			if (nextPageDelayCounter > NUM_CALLS_PAGE_CHANGE) {
				previousPage();
				nextPageDelayCounter = 0;
			}
			return;
		}

		readPosition.setY(newY);

		if (texDistance > 0) {
			textLayer.setY(texDistance);
		} else {
			// Start of page reached do nothing.
			if (readPosition.getY() <= 0.01) {
				return;
			}

			// Reached and of tex. Render new.
			// 1024 * 0.5 = 512. Thats the new y coordiante of the the new texture.
			ReadPosition newPos = new ReadPosition(readPosition.getPage(),
					lastRenderPosition.getX(),
					readPosition.getY() - textureSize / 2);
			createTexture(newPos);
			textLayer.setY(0.5f);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f", readPosition.getX(),
				readPosition.getY(), textLayer.getX(), textLayer.getY()));
	}

	void down(float speedFactor) {

		if (!shouldRenderFrame()) {
			return;
		}

		calculateMovedDistance(speedFactor);

		lastRenderTime = System.currentTimeMillis();

		float newY = readPosition.getY() + distance;
		float texDistance = textLayer.getY() + distance / textureSize;

		tempReadPosition.set(readPosition);
		tempReadPosition.setY(newY + textureSize / 4);

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

	void left(float speedFactor) {
		if (!shouldRenderFrame()) {
			return;
		}

		calculateMovedDistance(speedFactor);

		lastRenderTime = System.currentTimeMillis();

		readPosition.setX(readPosition.getX() - distance);
		float texDistance = textLayer.getX() - distance / textureSize;

		if (texDistance > 0) {
			textLayer.setX(texDistance);
		} else {

			// Start of page reached do nothing.
			if (readPosition.getX() <= 0.01) {
				return;
			}

			// Reached and of tex. Render new.
			ReadPosition newPos = new ReadPosition(readPosition.getPage(), readPosition.getX() - textureSize / 2,
					lastRenderPosition.getY());
			createTexture(newPos);
			textLayer.setX(0.5f);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f", readPosition.getX(),
				readPosition.getY(), textLayer.getX(), textLayer.getY()));
	}

	void right(float speedFactor) {
		if (!shouldRenderFrame()) {
			return;
		}

		calculateMovedDistance(speedFactor);

		lastRenderTime = System.currentTimeMillis();

		float newX = readPosition.getX() + distance;
		float texDistance = textLayer.getX() + distance / textureSize;

		tempReadPosition.set(readPosition);
		// Dive by 2 so we get 1/4 tex size overshoot to the border of
		// the document.
		tempReadPosition.setX(newX + textureSize / 4);

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

	ReadPosition getReadPosition() {
		return readPosition;
	}

	/**
	 * Manually switch to the next page.
	 */
	void nextPage() {
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
	void previousPage() {
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
