package de.fraunhofer.ipa.vrread;

import android.util.Log;

import de.fraunhofer.ipa.vrread.control.HeadGesture;

/**
 * Recognizes head gestures and transform them into read commands.
 * These commands are used to calculcate the current
 * position on the datasource and lead to creation of new textures and movement.
 *
 * Created by tbf on 01.03.2017.
 */
public class HeadGestureReadController extends ReadController {

	private final String TAG = HeadGestureReadController.class.getSimpleName();

	/**
	 * Translates a recognized head gesture into read commands.
	 *
	 * @param gesture The recognized head gesture.
	 */
	public void onHeadGesture(HeadGesture gesture) {
		Log.d(TAG, String.format("Recognized head gesture: %s", gesture));

	}
}
