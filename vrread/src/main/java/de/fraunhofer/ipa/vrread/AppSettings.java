package de.fraunhofer.ipa.vrread;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Objects;

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
		int zoom = sharedPrefs.getInt(getKey(R.string.key_pref_zoom), 1);
		return zoom;
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
