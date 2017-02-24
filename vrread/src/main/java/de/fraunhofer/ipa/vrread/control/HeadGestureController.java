package de.fraunhofer.ipa.vrread.control;

import com.google.vr.sdk.base.HeadTransform;

/**
 * Created by tbf on 24.02.2017.
 */

public class HeadGestureController implements GestureController {

	/**
	 * Holds the rotation of the head.
	 */
	private float[] headQuaternion = new float[4];
	private float[] eulerAngles = new float[3];

	@Override
	public void onHeadMovement(HeadTransform headTransform) {

		// Get deviation of the head angle from the z direction with regards to the y axis.
		headTransform.getQuaternion(headQuaternion, 0);
		calculateEulerAngles();

	}


	/**
	 * Caluclates the new euler angles from the head rotation quaternion.
	 */
	private void calculateEulerAngles() {

		final double psi = Math.atan2(-2. * (headQuaternion[2] * headQuaternion[3] - headQuaternion[0] *
				headQuaternion[1]), headQuaternion[0] * headQuaternion[0] - headQuaternion[1] * headQuaternion[1] -
				headQuaternion[2] * headQuaternion[2] + headQuaternion[3] * headQuaternion[3]);

		final double theta = Math.asin(2. * (headQuaternion[1] * headQuaternion[3] + headQuaternion[0] *
				headQuaternion[2]));
		final double phi = Math.atan2(2. * (headQuaternion[1] * headQuaternion[2] + headQuaternion[0] *
				headQuaternion[3]), headQuaternion[0] * headQuaternion[0] + headQuaternion[1] * headQuaternion[1] -
				headQuaternion[2] * headQuaternion[2] - headQuaternion[3] * headQuaternion[3]);

		eulerAngles[0] = (float) psi;
		eulerAngles[1] = (float) theta;
		eulerAngles[2] = (float) phi;

		//overlay.setText(String.format("Winkel\npsi: %f\ntheta: %f\nphi: %f", psi, theta, phi));
	}
}
