package de.fraunhofer.ipa.vrread.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import de.fraunhofer.ipa.vrread.ui.fragments.SettingsFragment;

/**
 * Created by tbf on 20.03.2017.
 */

public class SettingsActivity extends Activity {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}
}
