package de.fraunhofer.ipa.vrread.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.datasource.DatasourceService;


public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int INTENT_OPEN_DOC_CODE = 1;

	private DatasourceService datasourceService = new DatasourceService();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == INTENT_OPEN_DOC_CODE && resultCode == Activity.RESULT_OK) {
			// We were asked to open a document.

			Uri uri = null;
			if(data != null) {
				uri = data.getData();
				Log.d(TAG, "Open Document: " + uri.toString());

				// Prepare the datasource for this document and send it to the visualization.
				final Intent intent = new Intent();

			}
		}

	}

	/**
	 * Event from the activity if the user wants to open a file.
	 *
	 * @param view Trigger view.
	 */
	public void onFileOpen(View view) {

		// ACTION_OPEN_DOCUMENT is the intent to choose a certain file.
		final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");

		// Get the mime types from all installed Datasources.
		String[] mimetypes = datasourceService.getSupportedMimeTypes();
		//String[] mimetypes = {"image/*", "application/pdf"};
		intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

		startActivityForResult(intent, INTENT_OPEN_DOC_CODE);
	}
}