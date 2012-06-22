package com.jlyr.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

public class ResetDisplayPreference extends Preference implements Preference.OnPreferenceClickListener {

	public ResetDisplayPreference(Context context) {
		super(context);
		init(context, null);
	}

	public ResetDisplayPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ResetDisplayPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setOnPreferenceClickListener(this);
	}

	public boolean onPreferenceClick(Preference preference) {
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
		Editor editor = SP.edit();
		editor.remove("viewer_bg_color");
		editor.remove("viewer_font_color");
		editor.remove("viewer_font_size");
		editor.commit();

		return false;
	}
}