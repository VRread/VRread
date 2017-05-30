package de.fraunhofer.ipa.vrread.control;

import android.util.Log;

import com.google.vr.sdk.base.HeadTransform;

/**
 * The gesture controller detects certain head movements and controls the attached read controller.
 * Created by tbf on 24.02.2017.
 */

public class SimpleHeadGestureController implements GestureController {

	private final static String TAG = SimpleHeadGestureController.class.getSimpleName();

	/**
	 * The angle AFTER the threshold under which the speed is linearly ramped up.
	 */
	private final static float MAX_SPEED_ANGLE_PITCH_DEGREE = 10;
	private final static float MAX_SPEED_ANGLE_ROLL_DEGREE = 10;


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

	private int upMoveThreshold;
	private int downMoveThreshold;
	private int leftRightThreshold;

	private float speedFactorX = 1.0f;
	private float speedFactorY = 1.0f;

	private HeadGestureReadController controller;

	public SimpleHeadGestureController(SensitivityLevel sensitivity) {

		switch (sensitivity) {
			case HIGH:
				upMoveThreshold = 5;
				downMoveThreshold = -10;
				leftRightThreshold = 10;
				break;
			case MEDIUM:
				upMoveThreshold = 8;
				downMoveThreshold = -15;
				leftRightThreshold = 15;
				break;
			case LOW:
				upMoveThreshold = 10;
				downMoveThreshold = -18;
				leftRightThreshold = 20;
				break;
		}
	}

	private static float toRad(float degree) {
		degree = degree % 360;
		return (float) (degree * Math.PI / 180);
	}

	@Override
	public void onHeadMovement(HeadTransform headTransform) {

		// Get deviation of the head angle from the z direction with regards to the y axis.
		headTransform.getQuaternion(headQuaternion, 0);

		calculateEulerAngles();

		calculateSpeedFactors();

		if (roll < toRad(downMoveThreshold)) {
			if (controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_DOWN, speedFactorY);
			}
		} else if (roll > toRad(upMoveThreshold)) {
			if (controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_UP, speedFactorY);
			}
		}

		if (pitch > toRad(leftRightThreshold)) {
			if (controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_LEFT, speedFactorX);
			}
		} else if (pitch < toRad(-leftRightThreshold)) {
			if (controller != null) {
				controller.onHeadGesture(HeadGesture.LOOK_RIGHT, speedFactorX);
			}
		}
	}

	/**
	 * Depending on the euler angles the speed factor for both the x and y direction is calculated. If the angles of
	 * head movement increase a linear speed increase is performed until the speed factor reaches 1. Small movements
	 * of the head only perform a small speed increase.
	 */
	private void calculateSpeedFactors() {

		float overshootX = Math.abs(pitch) - toRad(leftRightThreshold);
		speedFactorX  = overshootX / toRad(MAX_SPEED_ANGLE_PITCH_DEGREE);

		if(speedFactorX < 0) {
			speedFactorX = 0;
		}
		if(speedFactorX > 1) {
			speedFactorX = 1;
		}

		if(roll < 0) {
			// Looked down
			// downMoveThresold is negative, roll is also negative we void the math abs with this term.
			float overshootY = Math.abs(roll) - Math.abs(toRad(downMoveThreshold));
			float maxSpdRoll = toRad(MAX_SPEED_ANGLE_ROLL_DEGREE);
			speedFactorY = Math.abs(overshootY) / maxSpdRoll;
		} else {
			// Looked up
			float overshootY = roll - toRad(upMoveThreshold);
			speedFactorY = overshootY / toRad(MAX_SPEED_ANGLE_ROLL_DEGREE);
		}

		if(speedFactorY > 1) {
			speedFactorY = 1;
		}
		if(speedFactorY < 0) {
			speedFactorY = 0;
		}

		Log.d(TAG, String.format("Speedfactors X: %.2f Y: %.2f", speedFactorX, speedFactorY));
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
