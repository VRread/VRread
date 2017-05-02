package de.fraunhofer.ipa.vrread;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Objects;

import de.fraunhofer.ipa.vrread.control.Contrast;
import de.fraunhofer.ipa.vrread.control.SensitivityLevel;

/**
 * Wrapper to access the user settings the user has created by editing the settings.
 * <p>
 * Created by tbf on 21.03.2017.
 */

public class AppSettings {

	private static final String KEY_FIRST_RUN = "first_run";

	private final Context ctx;
	private final SharedPreferences sharedPrefs;

	public AppSettings(Context ctx) {

		this.ctx = Objects.requireNonNull(ctx);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	private String getKey(int resId) {
		return ctx.getResources().getString(resId);
	}

	/**
	 * Returns the zoom factor of the app.
	 *
	 * @return The current zoom factor of the text.
	 */
	public float getZoomFactor() {
		return sharedPrefs.getInt(getKey(R.string.key_pref_zoom), 1);
	}

	/**
	 * Gives the user chosen sensitivity level of the device.
	 *
	 * @return The usable sensitivity level of the head movement towards the device.
	 */
	public SensitivityLevel getSensitivity() {

		final String sensitivity = sharedPrefs.getString(getKey(R.string.key_pref_sensitivity), "0");
		int level = Integer.parseInt(sensitivity);
		final int enumCount = SensitivityLevel.values().length;

		if (level < 0 || level >= enumCount) {
			throw new IndexOutOfBoundsException("Chosen sensitivty level is not in range.");
		}

		return SensitivityLevel.values()[level];
	}

	/**
	 * This returns the scroll speed multiplier.
	 *
	 * @return A value between 1 and 3.
	 */
	public int getScollspeedFactor() {
		int spd = Integer.parseInt(sharedPrefs.getString(getKey(R.string.key_pref_scrollspeed), "1"));

		if(spd < 1) {
			spd = 1;
		}
		if(spd > 3) {
			spd = 3;
		}
		return spd;
	}

	/**
	 * Returns the chosen contrast value.
	 *
	 * @return The user chosen contrast level.
	 */
	public Contrast getContrast() {

		final String sensitivity = sharedPrefs.getString(getKey(R.string.key_pref_contrast), "0");
		int level = Integer.parseInt(sensitivity);
		final int enumCount = Contrast.values().length;

		if (level < 0 || level >= enumCount) {
			throw new IndexOutOfBoundsException("Chosen sensitivty level is not in range.");
		}

		return Contrast.values()[level];
	}

	/**
	 * Returns if the user wants to have a helper line rendered.
	 *
	 * @return The helper line for display.
	 */
	public boolean hasHelperline() {
		return sharedPrefs.getBoolean(getKey(R.string.key_pref_helperline), false);
	}


	/**
	 * Returns TRUE if this is the first run of the application. After calling this method once. It will never return
	 * true again until the apps preferences are deleted.
	 *
	 * @return TRUE on the first call (first app run) FALSE afterwards.
	 */
	public boolean isFirstRun() {
		if (sharedPrefs.getBoolean(KEY_FIRST_RUN, true)) {
			sharedPrefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
			return true;
		}
		return false;
	}
}
