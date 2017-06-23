package de.fraunhofer.ipa.vrread.control;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Objects;

import de.fraunhofer.ipa.vrread.datasource.Datasource;
import de.fraunhofer.ipa.vrread.datasource.PDFDatasource;
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
	private final static float BASE_VELOCITY = 25f;

	// We sometimes need this size in order to calculate read distances.
	private final int textureSize;

	private final ScrollingTextLayer textLayer;
	private Datasource datasource;

	private long lastRenderTime = System.currentTimeMillis();

	private float renderDelay = 0f;
	private float scale = 1f;
	private ReadPosition currentReadPosition;
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
		this.currentReadPosition = new ReadPosition(0, 0, 0);

		if (textLayer.getTextureSize().getHeight() != textLayer.getTextureSize().getWidth()) {
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

		float newY = currentReadPosition.getY() - distance;
		float texDistance = textLayer.getY() - distance / textureSize;

		tempReadPosition.set(currentReadPosition);
		tempReadPosition.setY(newY);

		currentReadPosition.setY(newY);

		if (texDistance > 0) {
			textLayer.setY(texDistance);
		} else {
			// Start of page reached do nothing.
			if (currentReadPosition.getY() <= 0.01) {
				nextPageDelayCounter++;
				if (nextPageDelayCounter > NUM_CALLS_PAGE_CHANGE) {
					previousPage();
					nextPageDelayCounter = 0;
				}
				return;
			}

			// Reached and of tex. Render new.
			// 1024 * 0.5 = 512. Thats the new y coordiante of the the new texture.
			ReadPosition newPos = new ReadPosition(currentReadPosition.getPage(),
					lastRenderPosition.getX(),
					lastRenderPosition.getY() - textureSize / 2);
			createTexture(newPos);
			textLayer.setY(0.5f);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f",
				currentReadPosition.getX(),
				currentReadPosition.getY(),
				textLayer.getX(),
				textLayer.getY()));
	}

	void down(float speedFactor) {

		if (!shouldRenderFrame()) {
			return;
		}

		calculateMovedDistance(speedFactor);

		lastRenderTime = System.currentTimeMillis();

		float newY = currentReadPosition.getY() + distance;

		tempReadPosition.set(currentReadPosition);
		//tempReadPosition.setY(newY + textureSize / 4);
		tempReadPosition.setY(newY);

		if (!datasource.isInsidePage(tempReadPosition, scale)) {
			nextPageDelayCounter++;
			if (nextPageDelayCounter > NUM_CALLS_PAGE_CHANGE) {
				nextPage();
				nextPageDelayCounter = 0;
			}
			return;
		}

		currentReadPosition.setY(newY);

		float newShaderY = textLayer.getY() + distance / textureSize;

		if (newShaderY < 0.5) {
			textLayer.setY(newShaderY);
		} else {
			// Reached and of tex. Render new.
			ReadPosition newPos = new ReadPosition(currentReadPosition.getPage(),
					lastRenderPosition.getX(),
					currentReadPosition.getY());
			createTexture(newPos);
			textLayer.setY(0);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f",
				currentReadPosition.getX(),
				currentReadPosition.getY(),
				textLayer.getX(),
				textLayer.getY()));
	}

	void left(float speedFactor) {
		if (!shouldRenderFrame()) {
			return;
		}

		calculateMovedDistance(speedFactor);

		lastRenderTime = System.currentTimeMillis();

		final float newX = currentReadPosition.getX() - distance;
		currentReadPosition.setX(newX);

		float newShaderX = textLayer.getX() - distance / textureSize;

		if (newShaderX > 0) {
			textLayer.setX(newShaderX);
		} else {

			// Start of page reached do nothing.
			if (currentReadPosition.getX() <= 0.01) {
				return;
			}

			// Reached and of tex. Render new.
			ReadPosition newPosTex = new ReadPosition(currentReadPosition.getPage(),
					currentReadPosition.getX() - textureSize / 2,
					lastRenderPosition.getY());
			createTexture(newPosTex);
			textLayer.setX(0.5f);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f",
				currentReadPosition.getX(),
				currentReadPosition.getY(),
				textLayer.getX(),
				textLayer.getY()));
	}

	void right(float speedFactor) {
		if (!shouldRenderFrame()) {
			return;
		}

		calculateMovedDistance(speedFactor);

		lastRenderTime = System.currentTimeMillis();

		float newX = currentReadPosition.getX() + distance;
		float newShaderX = textLayer.getX() + distance / textureSize;

		tempReadPosition.set(currentReadPosition);
		// Dive by 2 so we get 1/4 tex size overshoot to the border of
		// the document.
		//tempReadPosition.setX(newX + textureSize / 4);
		tempReadPosition.setX(newX);

		if (!datasource.isInsidePage(tempReadPosition, scale)) {
			return;
		}

		currentReadPosition.setX(newX);

		if (newShaderX < 0.5) {
			textLayer.setX(newShaderX);
		} else {
			// Reached and of tex. Render new.
			ReadPosition newPos = new ReadPosition(currentReadPosition.getPage(), currentReadPosition.getX(),
					lastRenderPosition.getY());
			createTexture(newPos);
			textLayer.setX(0);
		}

		Log.d(TAG, String.format("RPosX: %.3f, RPosY: %.3f, TPosX: %.3f, TPosY: %.3f",
				currentReadPosition.getX(),
				currentReadPosition.getY(),
				textLayer.getX(),
				textLayer.getY()));
	}

	private void createTexture(ReadPosition texturePosition) {

		final int halfTextSize = (textureSize / 2);

		// Make sure next tex patch is increment of 512.
		final int y = (int) texturePosition.getY() / halfTextSize;
		texturePosition.setY(halfTextSize * y);

		final int x = (int) texturePosition.getX() / halfTextSize;
		texturePosition.setX(halfTextSize * x);

		Log.d(TAG, String.format("Create Tex nextTexPos: %.1f %.1f, readPos: %.1f %.1f textLayer: %.1f %.1f",
				texturePosition.getX(),
				texturePosition.getY(),
				currentReadPosition.getX(),
				currentReadPosition.getY(),
				textLayer.getX(),
				textLayer.getY()));

		lastRenderPosition.set(texturePosition);

		final TextureSize texSize = textLayer.getTextureSize();
		final Bitmap bitmap = datasource.getTextureBitmap(texturePosition, scale, texSize);
		textLayer.setTexture(bitmap);
	}

	ReadPosition getCurrentReadPosition() {
		return currentReadPosition;
	}

	/**
	 * Manually switch to the next page.
	 */
	void nextPage() {
		if (currentReadPosition.getPage() + 1 < datasource.getPageCount()) {
			currentReadPosition.setPage(currentReadPosition.getPage() + 1);
			// Set to top left position.
			currentReadPosition.setY(0);
			currentReadPosition.setX(0);
			textLayer.setX(0);
			textLayer.setY(0);

			createTexture(currentReadPosition);
		}
	}

	/**
	 * Manually switch to the previous page.
	 */
	void previousPage() {
		if (currentReadPosition.getPage() - 1 >= 0) {

			final int previousPage = currentReadPosition.getPage() - 1;

			final int pageHeight = ((PDFDatasource)datasource).getPageHeight(previousPage, scale);

			currentReadPosition.setPage(previousPage);
			currentReadPosition.setY(pageHeight);
			currentReadPosition.setX(0);

			ReadPosition newTexPos = new ReadPosition(
					previousPage,
					currentReadPosition.getX(),
					currentReadPosition.getY());

			textLayer.setY(0.5f);
			createTexture(newTexPos);
		}
	}

	/**
	 * Go to a specific page.
	 *
	 * @param page The page to jump to.
	 */
	public void gotoPage(int page) {
		if (page >= 0 && page < datasource.getPageCount()) {
			currentReadPosition.setPage(page);
			currentReadPosition.setY(0);
			currentReadPosition.setX(0);
			textLayer.setX(0);
			textLayer.setY(0);
			createTexture(currentReadPosition);
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
