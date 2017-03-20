package de.fraunhofer.ipa.vrread.ui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import de.fraunhofer.ipa.vrread.R;

/**
 * Fragment which will display the apps preferences/settings.
 * <p>
 * Created by tbf on 20.03.2017.
 */

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load preferences from XML resource.
		addPreferencesFromResource(R.xml.preferences);
	}
}
