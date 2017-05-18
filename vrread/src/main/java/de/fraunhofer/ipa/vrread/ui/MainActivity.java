package de.fraunhofer.ipa.vrread.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import de.fraunhofer.ipa.vrread.AppSettings;
import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.datasource.DatasourceService;


public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final int INTENT_OPEN_DOC_CODE = 1;
	// Callback constant to check for permission granting.
	private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;

	private DatasourceService datasourceService = new DatasourceService();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Prepare the default values for the preferences.
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		AppSettings appSettings = new AppSettings(this);

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

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
			grantResults) {

		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					Log.d(TAG, "Read permission granted.");

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					onFileOpen(null);

				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Log.d(TAG, "Permission for reading storage was not granted.");
				}
		}
	}

	/**
	 * Event from the activity if the user wants to open a file.
	 *
	 * @param view Trigger view.
	 */
	public void onFileOpen(View view) {

		// First check for permissions.
		int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

			} else {

				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
						MY_PERMISSIONS_REQUEST_READ_CONTACTS);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}

		// ACTION_OPEN_DOCUMENT is the intent to choose a certain file.
		final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		// prepare for multiple uris.
		intent.setType("*/*");
		//intent.setType("application/pdf");
		// Get the mime types from all installed Datasources.
		String[] mimetypes = datasourceService.getSupportedMimeTypes();
		intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

		startActivityForResult(intent, INTENT_OPEN_DOC_CODE);
	}

	/**
	 * TEMPORARLY: Opens the settings. Make this into a normal styled app lookup.
	 */
	public void onSettings(View view) {

		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}
