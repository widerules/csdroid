package org.jtb.csdroid;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class PrefsActivity extends PreferenceActivity {
	public static final int CHANGED_RESULT = 1;
	public static final int UNCHANGED_RESULT = 0;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);

		setResult(UNCHANGED_RESULT);
		
		ListPreference mc = (ListPreference) findPreference("maxCharts");
		mc.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
				setResult(CHANGED_RESULT);
				return true;
			}
		});
	}
}
