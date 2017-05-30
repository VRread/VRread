package de.fraunhofer.ipa.vrread.control;

import android.util.Log;

import de.fraunhofer.ipa.vrread.graphics.layer.ScrollingTextLayer;

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
	 * @param textLayer The textlayer to work upon when receiving the movement commands.
	 */
	public HeadGestureReadController(ScrollingTextLayer textLayer) {
		super(textLayer);
	}

	/**
	 * This method allow to place an additional speed factor which is used to control movement speed of the gesture
	 * controller.
	 *
	 * @param gesture The recognized head gesture.
	 * @param speedFactor A speed factor (between 0 and +INF) to controll the movement speed.
	 */
	public void onHeadGesture(HeadGesture gesture, float speedFactor) {
		Log.d(TAG, String.format("Recognized head gesture: %s", gesture));

		switch(gesture) {
			case LOOK_DOWN:
				down(speedFactor);
				break;
			case LOOK_UP:
				up(speedFactor);
				break;
			case LOOK_LEFT:
				left(speedFactor);
				break;
			case LOOK_RIGHT:
				right(speedFactor);
				break;
			default:
				Log.e(TAG, "Direction is not known.");
				break;
		}
	}
}
