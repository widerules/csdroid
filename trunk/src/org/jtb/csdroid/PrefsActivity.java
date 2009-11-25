package org.jtb.csdroid;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

public class PrefsActivity extends PreferenceActivity {
	static final int CHANGED_RESULT = 1;
	static final int UNCHANGED_RESULT = 0;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);

		
		// TODO: these don't work
		//setResult(UNCHANGED_RESULT);
		setResult(CHANGED_RESULT);
		
		ListPreference mc = (ListPreference) findPreference("maxCharts");
		mc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				setResult(CHANGED_RESULT);
				return true;
			}
		});
		ListPreference unitsList = (ListPreference) findPreference("units");
		mc.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				setResult(CHANGED_RESULT);
				return true;
			}
		});
	}
}
