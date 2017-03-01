package de.fraunhofer.ipa.vrread.control;

import java.util.Objects;

import de.fraunhofer.ipa.vrread.datasource.Datasource;
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

	}

	/**
	 * Manually switch to the previous page.
	 */
	public void previousPage() {

	}

	/**
	 * Go to a specific page.
	 *
	 * @param page The page to jump to.
	 */
	public void gotoPage(int page) {

	}

}
