package com.jlyr;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
 
public class JLyrSettings extends PreferenceActivity {
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.setDefaultValues(JLyrSettings.this, R.xml.settings, false);
    }
 
}