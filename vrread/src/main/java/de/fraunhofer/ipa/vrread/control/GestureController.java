package de.fraunhofer.ipa.vrread.control;

import com.google.vr.sdk.base.HeadTransform;

/**
 * Created by tbf on 24.02.2017.
 */

public interface GestureController {

	void onHeadMovement(HeadTransform headTransform);
}
