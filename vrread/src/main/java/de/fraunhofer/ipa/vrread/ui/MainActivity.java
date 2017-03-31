package de.fraunhofer.ipa.vrread.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import de.fraunhofer.ipa.vrread.AppSettings;
import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.datasource.DatasourceService;


public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int INTENT_OPEN_DOC_CODE = 1;

	private DatasourceService datasourceService = new DatasourceService();
	private AppSettings appSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Prepare the default values for the preferences.
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		appSettings = new AppSettings(this);

		if (appSettings.isFirstRun()) {
			// Show the settings activity with first run flag set.
			final Intent intent = new Intent(this, SettingsActivity.class);
			intent.putExtra(SettingsActivity.EXTRA_FIRST_RUN, Boolean.valueOf(true));
			startActivity(intent);
		}

		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == INTENT_OPEN_DOC_CODE && resultCode == Activity.RESULT_OK) {
			// We were asked to open a document.
			if (data != null) {
				Uri uri = data.getData();
				Log.d(TAG, "Open Document: " + uri.toString());

				// Prepare the datasource for this document and send it to the visualization.
				final Intent intent = new Intent(this, VRViewActivity.class);
				intent.putExtra(VRViewActivity.EXTRA_OPEN_URI, uri);
				startActivity(intent);

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
		// prepare for multiple uris.
		intent.setType("*/*");
		// Get the mime types from all installed Datasources.
		String[] mimetypes = datasourceService.getSupportedMimeTypes();
		intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

		startActivityForResult(intent, INTENT_OPEN_DOC_CODE);
	}

	/**
	 * TEMPORARLY: Opens the settings. Make this into a normal styled app lookup.
	 *
	 * @param view
	 */
	public void onSettings(View view) {

		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}
