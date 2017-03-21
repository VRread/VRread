package de.fraunhofer.ipa.vrread.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import de.fraunhofer.ipa.vrread.R;
import de.fraunhofer.ipa.vrread.ui.fragments.SettingsFragment;

/**
 * Displays the settings of the user.
 * Created by tbf on 20.03.2017.
 */

public class SettingsActivity extends Activity {

	public static final String EXTRA_FIRST_RUN = "de.fhg.ipa.vrread.EXT_FIRST_RUN";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getIntent().hasExtra(EXTRA_FIRST_RUN)) {
			setContentView(R.layout.settings_note);

			getFragmentManager().beginTransaction()
					.add(R.id.setote_setting_fragment, new SettingsFragment())
					.commit();
		} else {
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
		}
	}
}
