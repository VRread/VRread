package de.fraunhofer.ipa.vrread;

/**
 * Holds information about the size of a texture.
 * Created by tbf on 22.02.2017.
 */

public final class TextureSize {

	private final int width;
	private final int height;

	public TextureSize(int width, int height) {
		if(width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Width and height must be bigger then 0.");
		}

		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
