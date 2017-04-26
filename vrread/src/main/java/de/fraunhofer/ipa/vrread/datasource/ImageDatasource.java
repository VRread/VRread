package de.fraunhofer.ipa.vrread.datasource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import de.fraunhofer.ipa.vrread.R;

/**
 * Reads and gives access to an image resource.
 *
 * Created by tbf on 02.03.2017.
 */

public class ImageDatasource implements Datasource {

	private Bitmap bitmap;

	public ImageDatasource(Context ctx, int resId) {

		bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.text);
	}

	@SuppressWarnings("unused")
	public static String[] getSupportedMimeTypes() {
		return new String[]{"image/*"};
	}

	@Override
	public Bitmap getTextureBitmap(ReadPosition position, float scale, TextureSize size) {
		return bitmap;
	}

	@Override
	public int getPageCount() {
		return 0;
	}

	@Override
	public boolean isInsidePage(ReadPosition tempReadPosition, float scale) {
		return true;
	}
}
