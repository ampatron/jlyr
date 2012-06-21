package com.jlyr.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

public class ResetStringPreference extends Preference implements Preference.OnPreferenceClickListener {
	private String mResetKey = null;
	private String mDefaultValue = null;  

	private static final String androidns = "http://schemas.android.com/apk/res/android";

	public ResetStringPreference(Context context) {
		super(context);
		init(context, null);
	}

	public ResetStringPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ResetStringPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setOnPreferenceClickListener(this);
		if (attrs != null) {
			mResetKey = attrs.getAttributeValue(null, "resetKey");
			mDefaultValue = attrs.getAttributeValue(androidns, "defaultValue");
		}
	}

	public boolean onPreferenceClick(Preference preference) {
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
		Editor editor = SP.edit();
		if (mDefaultValue == null) {
			editor.remove(mResetKey);
		} else {
			editor.putString(mResetKey, mDefaultValue);
		}
		editor.commit();

		return false;
	}
}