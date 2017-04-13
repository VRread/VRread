package de.fraunhofer.ipa.vrread.datasource;

import java.util.Locale;

/**
 * Helper class to locate the current position inside a document.
 * It consists of the page, and the x and y coordinates.
 *
 * Created by Thomas Felix on 22.02.2017.
 */
public class ReadPosition {

	private int page;
	private float x;
	private float y;

	public ReadPosition() {
		setY(0);
		setX(0);
		setPage(0);
	}

	public ReadPosition(int page, float x, float y ) {
		setPage(page);
		setX(x);
		setY(y);
	}

	public void setPage(int page) {
		if(page < 0) {
			throw new IllegalArgumentException("Page can not be negative.");
		}
		this.page = page;
	}

	public void setX(float x) {
		if(x < 0) {
			throw new IllegalArgumentException("X can not be negative.");
		}

		this.x = x;
	}

	public void setY(float y) {
		if(y < 0) {
			throw new IllegalArgumentException("Y can not be negative.");
		}

		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public int getPage() {
		return page;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "ReadPos[x: %f, y: %f, page: %d]", getX(), getY(), getPage());
	}
}
