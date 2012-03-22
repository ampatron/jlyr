package com.jlyr.util;

import android.util.Log;

import com.jlyr.providers.*;

public class LyricFetcher {

	LyricsProvider[] mProviders = null;
	int mProviderIndex = 0;
	
	Track mTrack = null;
	String mResponse = null;
	GenericHandler mHandler = null;
	
	public static final String TAG = "JLyrFetcher";
	
	public LyricFetcher(Track track) {
		mTrack = track;
		loadProviders(null);
	}
	
	public LyricFetcher(Track track, String source) {
		mTrack = track;
		loadProviders(source);
	}
	
	private void loadProviders(String source) {
		if (source == null) {
			mProviders = new LyricsProvider[] {
					new DummyProvider(mTrack),
					new LyrDbProvider(mTrack),
					new ChartLyricsProvider(mTrack)};
		} else if (source == "ChartLyrics") {
			mProviders = new LyricsProvider[] {
					new ChartLyricsProvider(mTrack)};
		} else if (source == "LyrDB") {
			mProviders = new LyricsProvider[] {
					new LyrDbProvider(mTrack)};
		} else {
			Log.w(TAG, "Got an unkown source <" + source + ">. Using default.");
			mProviders = new LyricsProvider[] {
					new LyrDbProvider(mTrack)};
		}
	}
	
	public void fetchLyrics(GenericHandler _handler) {
		mHandler = _handler;
		mProviderIndex = -1;
		
		useNextProvider();
	}
	
	private void useNextProvider() {
		mProviderIndex++;
		if (mProviderIndex >= mProviders.length) {
			mHandler.handleSuccess();
			return;
		}
		
		LyricsProvider provider = mProviders[mProviderIndex];
		Log.d(TAG, "Trying provider " + provider.getSource());
		
		provider.loadLyrics(new GenericHandler() {
        	public void handleSuccess() {
        		mResponse = mProviders[mProviderIndex].getLyrics();
        		if (mResponse == null) {
        			useNextProvider();
        		} else {
        			mHandler.handleSuccess();
        		}
        	}
        });
	}
	
	public String getResponse() {
		return mResponse;
	}
}
