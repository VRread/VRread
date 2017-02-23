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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.graphics.Layer;
import de.fraunhofer.ipa.vrread.graphics.Renderer;

/**
 * A Google VR sample application. </p><p> The TreasureHunt scene consists of a planar ground grid and a floating
 * "treasure" cube. When the user looks at the cube, the cube will turn gold. While gold, the user can activate the
 * Cardboard trigger, which will in turn randomly reposition the cube.
 */
public class VRViewActivity extends GvrActivity {

	private static final String TAG = VRViewActivity.class.getSimpleName();

	private Vibrator vibrator;
	private GvrView gvrView;

	private Renderer renderer;

	/**
	 * Sets the viewMatrix to our GvrView and initializes the transformation matrices we will use to render our scene.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.renderer);

		gvrView = (GvrView) findViewById(R.id.gvr_view);
		if (gvrView.setAsyncReprojectionEnabled(true)) {
			// Async reprojection decouples the app framerate from the display framerate,
			// allowing immersive interaction even at the throttled clockrates set by
			// sustained performance mode.
			AndroidCompat.setSustainedPerformanceMode(this, true);
		}

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		renderer = new Renderer(gvrView);
		renderer.addLayer(0, new Layer(this));
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

		// Always give user feedback.
		vibrator.vibrate(50);
	}
}
