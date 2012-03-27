package com.jlyr.providers;

import com.jlyr.util.GenericHandler;
import com.jlyr.util.Track;

import android.util.Log;

public class DummyProvider extends LyricsProvider {
	
	public static final String TAG = "JLyrDummyProvider";
	
	public DummyProvider(Track track) {
		super(track);
	}
	
	public String getSource() {
		return "Dummy";
	}
	
	@Override
	public void loadLyrics(GenericHandler _handler) {
		mHandler = _handler;
		
		mLyrics = null;
		mHandler.handleSuccess();
	}
}
