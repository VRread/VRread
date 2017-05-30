package de.fraunhofer.ipa.vrread.control;

import com.google.vr.sdk.base.HeadTransform;

/**
 * This interface manages head movements and performs a head gesture detection.
 * <p>
 * Created by tbf on 24.02.2017.
 */

public interface GestureController {

	/**
	 * The method is called if a new head transformation arrives during each frame.
	 *
	 * @param headTransform The current head movement.
	 */
	void onHeadMovement(HeadTransform headTransform);
}
