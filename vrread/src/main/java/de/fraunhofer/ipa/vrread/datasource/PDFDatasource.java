package de.fraunhofer.ipa.vrread.datasource;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * The datasource is responsible for creation of new textures and sending them
 * <p>
 * Created by tbf on 02.03.2017.
 */

public class PDFDatasource implements Datasource {

	private final static String TAG = PDFDatasource.class.getSimpleName();
	private static final int BITMAP_PADDING = 0;

	private PdfRenderer renderer;
	private ReadPosition pageBoundary = new ReadPosition();

	public PDFDatasource(Uri file, Context context) throws IOException {

		final ContentResolver resolver = context.getContentResolver();
		final ParcelFileDescriptor parcFile = resolver.openFileDescriptor(file, "r");

		if (parcFile == null) {
			throw new IllegalArgumentException("Could not open given file URI.");
		}

		renderer = new PdfRenderer(parcFile);
	}

	public static String[] getSupportedMimeTypes() {
		return new String[]{"application/pdf"};
	}

	/**
	 * Closes all resources. After this call the datasource must not be used anymore.
	 */
	public void close() {
		renderer.close();
		renderer = null;
	}

	@Override
	public Bitmap getTextureBitmap(ReadPosition position, float scale, TextureSize size) {

		PdfRenderer.Page page = renderer.openPage(position.getPage());

		// Check if we must invalidate the boundary.
		pageBoundary.setPage(position.getPage());
		pageBoundary.setX(page.getWidth());
		pageBoundary.setY(page.getHeight());

		// We start to read at the top of the page, which is half of the PDF size.
		float curY = - position.getY() / scale;
		float curX = - position.getX() / scale;

		float maxOffsetX = - page.getWidth();
		float maxOffsetY = - page.getHeight();

		if(curX < maxOffsetX) {
			curX = maxOffsetX;
		}

		if(curY < maxOffsetY) {
			curY = maxOffsetY;
		}

		// Generate a bitmap with the correct dimensions.
		Rect bitmapRect = new Rect(BITMAP_PADDING, BITMAP_PADDING,
				size.getWidth() - BITMAP_PADDING,
				size.getHeight() - BITMAP_PADDING);
		Bitmap bitmap = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);

		Matrix transform = new Matrix();
		transform.postTranslate(curX, curY);
		transform.postScale(scale, scale);

		page.render(bitmap, bitmapRect, transform, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
		page.close();

		return bitmap;
	}

	@Override
	public int getPageCount() {
		return renderer.getPageCount();
	}

	@Override
	public boolean isInsidePage(ReadPosition tempReadPosition, float scale) {
		boolean isInPageX = tempReadPosition.getX() / scale < pageBoundary.getX();
		boolean isInPageY = tempReadPosition.getY() / scale < pageBoundary.getY();
		return isInPageX && isInPageY;
	}
}
