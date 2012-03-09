package com.jlyr.util;

import android.util.Log;

import com.jlyr.providers.*;

public class LyricFetcher {

	LyricsProvider mProvider = null;
	Track mTrack = null;
	
	public static final String TAG = "JLyrFetcher";
	
	public LyricFetcher(Track track) {
		mTrack = track;
	}
	
	public LyricFetcher(Track track, String source) {
		mTrack = track;
		if (source == "ChartLyrics") {
			mProvider = new ChartLyricsProvider(mTrack);
		} else if (source == "LyrDB") {
			mProvider = new LyrDbProvider(mTrack);
		} else if (source != null) {
			Log.w(TAG, "Got an unkown source " + source + ". Using default.");
			mProvider = new LyrDbProvider(mTrack);
		}
	}
	
	public String fetchLyrics() {
		String response = null;
		if (mProvider != null) {
			response = mProvider.getLyrics();
		} else {
			LyricsProvider[] providers = new LyricsProvider[] {
					new LyrDbProvider(mTrack),
					new ChartLyricsProvider(mTrack)};
			for (int i=0; i<providers.length; i++) {
				Log.d(TAG, "Trying provider " + providers[i].toString());
				response = providers[i].getLyrics();
				if (response == null) {
					Log.w(TAG, "Got a null response from " + providers[i].toString());
				} else {
					break;
				}
			}
		}
		return response;
	}
}
