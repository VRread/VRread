package de.fraunhofer.ipa.vrread;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

/**
 * Checks if the device supports needed features in order to operate the app.
 * <p>
 * tbf on 11.07.2017.
 */

public class CapabilityChecker implements DialogInterface.OnDismissListener {

	private final Context ctx;

	public CapabilityChecker(Context ctx) {

		this.ctx = Objects.requireNonNull(ctx);
	}

	/**
	 * Checks if the currently used device provides all needed features for this app to function normally.
	 *
	 * @return TRUE if the app uses all the features. FALSE otherwise.
	 */
	public boolean hasNeededFeatures() {

		final SensorManager sensorMgr = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
		final List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL);

		for (Sensor sensor : sensors) {
			if (sensor.getName().contains("Gyroscope")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This checks the capability and also informs the user if the device does not meet all requirements.
	 *
	 * @return TRUE if the app uses all the features. FALSE otherwise.
	 */
	public void notifyNotAllFeatures() {
		if(!hasNeededFeatures()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

			builder.setMessage(R.string.device_misses_features)
					.setTitle(R.string.attention)
					.setPositiveButton(android.R.string.ok, null);

			builder.setOnDismissListener(this);

			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	/**
	 * Terminates the androit app.
	 * @param dialogInterface
	 */
	@Override
	public void onDismiss(DialogInterface dialogInterface) {
		System.exit(0);
	}
}
