package de.fraunhofer.ipa.vrread.datasource;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class which uses and performs operations upon the available datasource implementations.
 * Created by tbf on 20.03.2017.
 */

public class DatasourceService {

	private static final String TAG = DatasourceService.class.getSimpleName();
	private static final String DATASOURCE_PKG = "de.fraunhofer.ipa.vrread";
	private static final String MIME_METHOD_NAME = "getSupportedMimeTypes";

	private static final Class[] CLAZZES = new Class[] {
			PDFDatasource.class,
			//ImageDatasource.class
	};

	/**
	 * Returns a list of all mime types supported by the implemented Datasources.
	 *
	 * @return A array of mimetypes which are supported by the implementations.
	 */
	@SuppressWarnings("unchecked")
	public String[] getSupportedMimeTypes() {

		// Get all classes implementing datasource.
		List<String> mimes = new ArrayList<>();

		for (Class clazz : CLAZZES) {
			try {

				final Method method = clazz.getMethod(MIME_METHOD_NAME);
				String[] supportedMimes = (String[]) method.invoke(null);
				mimes.addAll(Arrays.asList(supportedMimes));

			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException  e) {
				Log.e(TAG, "Error while extracting supported mime types.", e);
			}
		}

		return mimes.toArray(new String[]{});
	}
}
