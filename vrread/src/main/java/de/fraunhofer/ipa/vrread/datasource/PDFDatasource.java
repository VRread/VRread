package de.fraunhofer.ipa.vrread.datasource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The datasource is responsible for creation of new textures and sending them
 * <p>
 * Created by tbf on 02.03.2017.
 */

public class PDFDatasource implements Datasource {

	private final static String TAG = PDFDatasource.class.getSimpleName();
	private static final int BITMAP_PADDING = 0;

	private PdfRenderer renderer;
	private Context ctx;

	public PDFDatasource(Uri file, Context context) throws IOException {

		final ParcelFileDescriptor parcFile = context.getContentResolver().openFileDescriptor(file, "r");

		if (parcFile == null) {
			throw new IllegalArgumentException("Could not open given file URI.");
		}

		renderer = new PdfRenderer(parcFile);

		this.ctx = context;
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

		// We start to read at the top of the page, which is half of the PDF size.
		float curY = -(page.getHeight() - size.getHeight() / scale) * position.getY() * 1.256f;
		float curX = -(page.getWidth() - size.getWidth() / scale) * position.getX() * 1.256f;

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

		saveFile(bitmap);

		return bitmap;
	}

	private void saveFile(Bitmap img) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"vrread");
		mediaStorageDir.mkdirs();

		// Create a media file name
		String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
		String mImageName = "MI_" + timeStamp + ".png";
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);

		try {
			FileOutputStream fos = new FileOutputStream(mediaFile);
			img.compress(Bitmap.CompressFormat.PNG, 90, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
	}

	@Override
	public int getPageCount() {
		return renderer.getPageCount();
	}
}
