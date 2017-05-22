package de.fraunhofer.ipa.vrread.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.ui.fragments.SettingsFragment;

/**
 * Displays the settings of the user. On the first run of the app it will just add the settings fragment to an
 * additional explaination text. On the later calls the description text wont be shown anymore.
 * Created by tbf on 20.03.2017.
 */

public class SettingsActivity extends Activity {

	public static final String EXTRA_FIRST_RUN = "de.fhg.ipa.vrread.EXT_FIRST_RUN";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getIntent().hasExtra(EXTRA_FIRST_RUN)) {
			setContentView(R.layout.settings_note);

			// Add the settings fragment to the description text.
			getFragmentManager().beginTransaction()
					.add(R.id.setote_setting_fragment, new SettingsFragment())
					.commit();
		} else {
			// Replace the whole content (and the descriptive text) with the settings fragment.
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
		}
	}
}
