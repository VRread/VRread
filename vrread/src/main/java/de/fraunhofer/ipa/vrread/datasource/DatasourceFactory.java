package de.fraunhofer.ipa.vrread.datasource;

import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;

/**
 * The {@link DatasourceFactory} creates datasources upon the given file URI.
 * <p>
 * Created by tbf on 21.03.2017.
 */

public class DatasourceFactory {

	private static final String TAG = DatasourceFactory.class.getSimpleName();

	public DatasourceFactory() {

	}

	/**
	 * Returns the mime type of the given file url.
	 *
	 * @param url A url pointing to a file.
	 * @return The mime type of this file.
	 */
	private String getMimeType(String url) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}

		return null;
	}

	/**
	 * Creates a suitable datasource for the given file uri. If no suitable datasource could be found then null is
	 * returned.
	 *
	 * @param fileUri The Uri of the file.
	 * @return Returns a setup datasource for this kind of file or null of no suitable datasource could have been
	 * found.
	 */
	public Datasource getDatasource(Uri fileUri) {

		// Check which filetype is contained inside the uri.
		final File file = new File(fileUri.getPath());
		String mime = getMimeType(file.toString());

		if(PDFDatasource.getSupportedMimeTypes().equals(mime)) {
			return createPDFSource(file);
		} else {
			return null;
		}
	}

	private PDFDatasource createPDFSource(File file) {

		try {
			return new PDFDatasource(file);
		} catch (IOException e) {
			Log.e(TAG, "Could not open PDF file.", e);
			return null;
		}
	}
}
