package com.jlyr.preference;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

public class ProvidersPreference extends Preference implements Preference.OnPreferenceClickListener {

	public ProvidersPreference(Context context) {
		super(context);
		init(context, null);
	}

	public ProvidersPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public ProvidersPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setOnPreferenceClickListener(this);
	}

	public boolean onPreferenceClick(Preference preference) {
		Intent i = new Intent(getContext(), JLyrSourceSelector.class);
		i.putExtra("key", getKey());
		getContext().startActivity(i);

		return false;
	}
}
