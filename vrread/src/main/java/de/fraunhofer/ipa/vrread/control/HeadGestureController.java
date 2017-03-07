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

	// roll X-Axis
	private float roll;

	// Pitch y-axis
	private float pitch;

	// yaw z-axis
	private float yaw;

	private HeadGestureReadController controller;

	private static float toRad(float degree) {
		degree = degree % 360;
		return (float)(degree * Math.PI / 180);
	}

	@Override
	public void onHeadMovement(HeadTransform headTransform) {

		// Get deviation of the head angle from the z direction with regards to the y axis.
		headTransform.getQuaternion(headQuaternion, 0);
		calculateEulerAngles();

		if(roll > toRad(5)) {
			if(controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_DOWN);
			}
		} else if(roll < toRad(-10)) {
			if(controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_UP);
			}
		}

		if(pitch > toRad(10)) {
			if(controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_LEFT);
			}
		}else if(pitch < toRad(-10)) {
			if(controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_RIGHT);
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

		double ysqr = headQuaternion[1] * headQuaternion[1];

		double t0 = 2.0 * (headQuaternion[3] * headQuaternion[0] + headQuaternion[1] * headQuaternion[2]);
		double t1 = 1.0 - 2.0 * (headQuaternion[0] * headQuaternion[0] + ysqr);

		roll = (float) Math.atan2(t0, t1);

		double t2 = 2.0 * (headQuaternion[3] * headQuaternion[1] - headQuaternion[2] * headQuaternion[0]);
		t2 = t2 > 1.0 ? 1.0 : t2;
		t2 = t2 < -1.0 ? -1.0 : t2;

		pitch = (float) Math.asin(t2);

		double t3 = 2.0 * (headQuaternion[3] * headQuaternion[2] + headQuaternion[0] * headQuaternion[1]);
		double t4 = 1.0 - 2.0 * (ysqr + headQuaternion[2] * headQuaternion[2]);
		yaw = (float) Math.atan2(t3, t4);

		Log.d(TAG, String.format("Euler angles roll: %.3f, pitch: %.3f, yaw: %.3f", roll, pitch, yaw));
	}
}
