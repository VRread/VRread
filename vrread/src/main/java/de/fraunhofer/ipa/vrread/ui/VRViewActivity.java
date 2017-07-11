/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fraunhofer.ipa.vrread.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

import de.fraunhofer.ipa.vrread.AppSettings;
import de.fraunhofer.ipa.vrread.CapabilityChecker;
import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.control.HeadGestureReadController;
import de.fraunhofer.ipa.vrread.control.SensitivityLevel;
import de.fraunhofer.ipa.vrread.control.SimpleHeadGestureController;
import de.fraunhofer.ipa.vrread.datasource.Datasource;
import de.fraunhofer.ipa.vrread.datasource.DatasourceFactory;
import de.fraunhofer.ipa.vrread.graphics.Renderer;
import de.fraunhofer.ipa.vrread.graphics.layer.HelperLineLayer;
import de.fraunhofer.ipa.vrread.graphics.layer.ScrollingTextLayer;

/**
 * A Google VR sample application. </p><p> The TreasureHunt scene consists of a planar ground grid and a floating
 * "treasure" cube. When the user looks at the cube, the cube will turn gold. While gold, the user can activate the
 * Cardboard trigger, which will in turn randomly reposition the cube.
 */
public class VRViewActivity extends GvrActivity {

	private static final String TAG = VRViewActivity.class.getSimpleName();

	/**
	 * Extra URI to open a file upon start of this activity. The filetype must be supported by one of the implemented
	 * {@link Datasource}.
	 */
	public static final String EXTRA_OPEN_URI = "de.fhg.ipa.vrread.openfile";

	private GvrView gvrView;
	private Renderer renderer;
	private AppSettings appSettings;
	private DatasourceFactory datasourceFactory;
	private HeadGestureReadController readController;

	/**
	 * Sets the viewMatrix to our GvrView and initializes the transformation matrices we will use to render our scene.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Check if the app has all the features needes for it to work properly.
		CapabilityChecker checker = new CapabilityChecker(this);
		checker.notifyNotAllFeatures();

		setContentView(R.layout.renderer);

		appSettings = new AppSettings(this);
		datasourceFactory = new DatasourceFactory(this);

		gvrView = (GvrView) findViewById(R.id.gvr_view);
		if (gvrView.setAsyncReprojectionEnabled(true)) {
			// Async reprojection decouples the app framerate from the display framerate,
			// allowing immersive interaction even at the throttled clockrates set by
			// sustained performance mode.
			AndroidCompat.setSustainedPerformanceMode(this, true);
		}

		renderer = new Renderer(gvrView);
		final ScrollingTextLayer textLayer = new ScrollingTextLayer(this);
		renderer.addLayer(0, textLayer);

		// Set the user chosen contrast mode.
		textLayer.setContrastMode(appSettings.getContrast());

		// Add the helper line if requested via settings.
		if (appSettings.hasHelperline()) {
			HelperLineLayer helperLineLayer = new HelperLineLayer(this);
			helperLineLayer.setLinePosition(appSettings.getHelperlinePosition());
			renderer.addLayer(1, helperLineLayer);
		}

		// Now we need a head gesture controller which finds certain
		// gestures in the movement.
		final SensitivityLevel level = appSettings.getSensitivity();
		final SimpleHeadGestureController headController = new SimpleHeadGestureController(level);
		renderer.setGestureController(headController);

		// Then we create the read controller which in turn will move the text layer upon the
		// movement of the head.
		readController = new HeadGestureReadController(textLayer);
		headController.setHeadGestureReadController(readController);

		// Adapt to the zoom factor.
		float zoomFac = appSettings.getZoomFactor();
		readController.setScale(zoomFac);
		readController.setScrollSpeedFactor(appSettings.getScollspeedFactor());

		// Check how our activity was started. If it was started via our main activity
		// open the file.
		if (getIntent().hasExtra(EXTRA_OPEN_URI)) {

			Uri fileUri = getIntent().getExtras().getParcelable(EXTRA_OPEN_URI);
			Log.d(TAG, "Received uri: " + fileUri);

			prepareDatasource(fileUri);
		} else {
			// Seems like we where started by a intent filter.
			Log.d(TAG, "Activity started by external intent. Trying to fetch data.");

			Uri data = getExternalIntentData(getIntent());

			if (data == null) {
				Toast.makeText(this, R.string.can_not_open_file, Toast.LENGTH_LONG).show();
				finish();
			}

			prepareDatasource(data);
		}
	}

	private void prepareDatasource(Uri fileUri) {
		Log.d(TAG, "Opening URI for datasource.");

		final Datasource ds = datasourceFactory.getDatasource(fileUri);

		if (ds != null) {
			readController.setDatasource(ds);
			readController.gotoPage(0);
		} else {
			Log.e(TAG, "Can not open the given file URI.");
			Toast.makeText(this, getText(R.string.can_not_open_file), Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * Check which intent format triggered the application. We will display this data here.
	 *
	 * @param intent The intent which triggered the application.
	 * @return The URI of the file to open.
	 */
	private Uri getExternalIntentData(Intent intent) {

		switch(intent.getAction()) {
			case Intent.ACTION_VIEW:
				return  intent.getData();
			case Intent.ACTION_SEND:
				// We got arbitrary text send. It is most likly a URL.
				final String text = intent.getStringExtra(Intent.EXTRA_TEXT);
				final Uri dataUri = Uri.parse(text);
				return dataUri;
			default:
				// No known data.
				return null;
		}
	}

	/**
	 * Handle the sound button keys and perform the reset to zero and jumping back to the beginning of the document.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			Log.d(TAG, "Volume UP was pressed");
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			Log.d(TAG, "Volume DOWN was pressed.");
			readController.gotoPage(0);
			gvrView.recenterHeadTracker();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}


	/**
	 * Called when the Cardboard trigger is pulled.
	 */
	@Override
	public void onCardboardTrigger() {
		Log.i(TAG, "onCardboardTrigger");
	}

	/**
	 * Resets the reader back to the beginning of the file.
	 */
	public void onJumpStart(View view) {
		Log.d(TAG, "Resetting view to page 0.");
		readController.gotoPage(0);
	}
}
