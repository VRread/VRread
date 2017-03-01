package de.fraunhofer.ipa.vrread.control;

import android.util.Log;

import com.google.vr.sdk.base.HeadTransform;

/**
 * Created by tbf on 24.02.2017.
 */

public class HeadGestureController implements GestureController {

	private final static String TAG = HeadGestureController.class.getSimpleName();

	/**
	 * Holds the rotation of the head.
	 */
	private float[] headQuaternion = new float[4];

	private float psi;
	private float theta;
	private float phi;

	private HeadGestureReadController controller;

	@Override
	public void onHeadMovement(HeadTransform headTransform) {

		// Get deviation of the head angle from the z direction with regards to the y axis.
		headTransform.getQuaternion(headQuaternion, 0);
		calculateEulerAngles();

		if(Math.abs(psi) > 0.2f) {
			if(controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_LEFT);
			}
		}
	}

	/**
	 * Sets a callback so the gesture is actually transformed into a reading display operation.
	 *
	 * @param controller The new {@link HeadGestureReadController} handling the read event.
	 */
	public void setHeadGestureReadController(HeadGestureReadController controller) {
		this.controller = controller;
	}

	/**
	 * Caluclates the new euler angles from the head rotation quaternion.
	 */
	private void calculateEulerAngles() {

		psi = (float) Math.atan2(-2. * (headQuaternion[2] * headQuaternion[3] - headQuaternion[0] *
				headQuaternion[1]), headQuaternion[0] * headQuaternion[0] - headQuaternion[1] * headQuaternion[1] -
				headQuaternion[2] * headQuaternion[2] + headQuaternion[3] * headQuaternion[3]);

		theta =(float) Math.asin(2. * (headQuaternion[1] * headQuaternion[3] + headQuaternion[0] *
				headQuaternion[2]));

		phi = (float) Math.atan2(2. * (headQuaternion[1] * headQuaternion[2] + headQuaternion[0] *
				headQuaternion[3]), headQuaternion[0] * headQuaternion[0] + headQuaternion[1] * headQuaternion[1] -
				headQuaternion[2] * headQuaternion[2] - headQuaternion[3] * headQuaternion[3]);

		Log.d(TAG, String.format("Euler angles psi: %f, theta: %f, phi: %f", psi, theta, phi));
	}
}
