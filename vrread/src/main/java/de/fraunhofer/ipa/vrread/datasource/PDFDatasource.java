package de.fraunhofer.ipa.vrread.datasource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.IOException;

/**
 * The datasource is responsible for creation of new textures and sending them
 * <p>
 * Created by tbf on 02.03.2017.
 */

public class PDFDatasource implements Datasource {

	private PdfRenderer renderer;

	public PDFDatasource(Uri file, Context context) throws IOException {

		final ParcelFileDescriptor parcFile = context.getContentResolver().openFileDescriptor(file, "r");
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

		// Generate a bitmap with the correct dimensions.
		Bitmap bitmap = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);

		Matrix transform = new Matrix();
		transform.postScale(scale, scale);
		transform.postTranslate(position.getX(), position.getY());

		page.render(bitmap, null, transform, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
		page.close();

		return bitmap;
	}

	@Override
	public int getPageCount() {
		return renderer.getPageCount();
	}
}
