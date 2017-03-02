package de.fraunhofer.ipa.vrread.control;

import android.graphics.Bitmap;

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

	private Datasource datasource;
	private final ScrollingTextLayer textLayer;
	/**
	 * Distance to be scrolled when a looking method is called.
	 */
	private int scrollDistanceIncrement = 1;

	private int currentPage = 0;
	private int x;
	private int y;
	private float scale = 1f;

	/**
	 * @param textLayer The textlayer to work upon when receiving the movement commands.
	 */
	ReadController(ScrollingTextLayer textLayer) {

		this.textLayer = Objects.requireNonNull(textLayer);
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
		textLayer.setY(textLayer.getY() + scrollDistanceIncrement);
	}

	public void down() {
		textLayer.setY(textLayer.getY() - scrollDistanceIncrement);
	}

	public void left() {
		textLayer.setX(textLayer.getX() - scrollDistanceIncrement);
	}

	public void right() {
		textLayer.setX(textLayer.getX() + scrollDistanceIncrement);
	}

	/**
	 * Manually switch to the next page.
	 */
	public void nextPage() {
		if(currentPage + 1 < datasource.getPageCount()) {
			currentPage++;
			x = 0;
			y = 0;
			final TextureSize texSize = textLayer.getTextureSize();
			final Bitmap bitmap = datasource.getTextureBitmap(new ReadPosition(currentPage, x, y), scale, texSize);
			textLayer.setTexture(bitmap);
			textLayer.setX(x);
			textLayer.setY(y);
		}
	}

	/**
	 * Manually switch to the previous page.
	 */
	public void previousPage() {
		if(currentPage - 1 <= 0) {
			currentPage--;
			x = 0;
			y = 0;
			final TextureSize texSize = textLayer.getTextureSize();
			final Bitmap bitmap = datasource.getTextureBitmap(new ReadPosition(currentPage, x, y), scale, texSize);
			textLayer.setTexture(bitmap);
			textLayer.setY(y);
			textLayer.setX(x);
		}
	}

	/**
	 * Go to a specific page.
	 *
	 * @param page The page to jump to.
	 */
	public void gotoPage(int page) {
		if(page >= 0 && page < datasource.getPageCount()) {
			currentPage = page;
			x = 0;
			y = 0;
			final TextureSize texSize = textLayer.getTextureSize();
			final Bitmap bitmap = datasource.getTextureBitmap(new ReadPosition(currentPage, x, y), scale, texSize);
			textLayer.setTexture(bitmap);
			textLayer.setY(y);
			textLayer.setX(x);
		}
	}

}
