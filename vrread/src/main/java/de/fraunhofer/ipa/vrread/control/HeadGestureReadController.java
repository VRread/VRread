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

	private int pageCounter = 0;

	/**
	 * @param textLayer The textlayer to work upon when receiving the movement commands.
	 */
	public HeadGestureReadController(ScrollingTextLayer textLayer) {
		super(textLayer);
	}

	/**
	 * Translates a recognized head gesture into read commands.
	 *
	 * @param gesture The recognized head gesture.
	 */
	public void onHeadGesture(HeadGesture gesture) {
		Log.d(TAG, String.format("Recognized head gesture: %s", gesture));

		switch(gesture) {
			case LOOK_DOWN:

				// Check if we need to flip the page.
				if(getReadPosition().getY() >= 0.99f) {
					pageCounter++;
					if(pageCounter >= 10) {
						pageCounter = 0;
						nextPage();
					}
				} else {
					pageCounter = 0;
				}

				down();
				break;
			case LOOK_UP:

				// Check if we need to flip the page.
				if(getReadPosition().getY() <= 0.01f) {
					pageCounter++;
					if(pageCounter >= 10) {
						pageCounter = 0;
						previousPage();
					}
				} else {
					pageCounter = 0;
				}

				up();
				break;
			case LOOK_LEFT:
				left();
				break;
			case LOOK_RIGHT:
				right();
				break;
			default:
				Log.e(TAG, "Direction is not known.");
				break;
		}
	}
}
