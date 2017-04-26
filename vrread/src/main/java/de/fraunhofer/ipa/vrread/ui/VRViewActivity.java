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

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

import de.fraunhofer.ipa.vrread.AppSettings;
import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.control.HeadGestureController;
import de.fraunhofer.ipa.vrread.control.HeadGestureReadController;
import de.fraunhofer.ipa.vrread.control.SensitivityLevel;
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

	/**
	 * Sets the viewMatrix to our GvrView and initializes the transformation matrices we will use to render our scene.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
			renderer.addLayer(1, new HelperLineLayer(this));
		}

		// Now we need a head gesture controller which finds certain
		// gestures in the movement.
		final SensitivityLevel level = appSettings.getSensitivity();
		final HeadGestureController headController = new HeadGestureController(level);
		renderer.setGestureController(headController);

		// Then we create the read controller which in turn will move the text layer upon the
		// movement of the head.
		final HeadGestureReadController readController = new HeadGestureReadController(textLayer);
		headController.setHeadGestureReadController(readController);

		// Extract the file uri.
		if (getIntent().hasExtra(EXTRA_OPEN_URI)) {

			Uri fileUri = getIntent().getExtras().getParcelable(EXTRA_OPEN_URI);
			Log.d(TAG, "Received uri: " + fileUri);

			final Datasource ds = datasourceFactory.getDatasource(fileUri);

			if (ds != null) {
				// Adapt to the zoom factor.
				float zoomFac = appSettings.getZoomFactor();

				readController.setDatasource(ds);
				readController.setScale(zoomFac);
				readController.gotoPage(0);
			} else {
				Log.e(TAG, "Can not open the given file URI.");
			}
		}

		// Start intercepting power key
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG);
	}

	/**
	 * Handle the sound button keys and perform certain actions with them.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			Log.d(TAG, "Volume UP was pressed");
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			Log.d(TAG, "Volume DOWN was pressed.");
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
}
