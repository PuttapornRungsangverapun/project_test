package com.example.por.project_test;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by User on 24/4/2560.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.setting);
    }
}
