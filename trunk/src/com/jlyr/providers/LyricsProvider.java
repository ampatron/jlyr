package com.jlyr.providers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.jlyr.util.GenericHandler;
import com.jlyr.util.Track;

public abstract class LyricsProvider {
	
	protected Track mTrack = null;
	protected String mLyrics = null;
	protected GenericHandler mHandler = null;
	
	public static final String TAG = "JLyrProvider";
	
	public LyricsProvider(Track track) {
		mTrack = track;
	}
	
	public abstract String getSource();
	
	public String getLyrics() {
		return mLyrics;
	}
	
	public abstract void loadLyrics(GenericHandler _handler);
	
	protected static String enc(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "URLEncoder lacks support for UTF-8!?");
			return null;
		}
	}
}
