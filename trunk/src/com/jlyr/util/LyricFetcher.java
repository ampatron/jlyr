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
			// All providers
			loadProvidersFromList(null);
		} else {
			loadProvidersFromList(new String[] {source});
		}
	}
	
	private void loadProvidersFromList(String[] providers) {
		ProvidersCollection coll = new ProvidersCollection(providers);
		mProviders = coll.toArray(mTrack);
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
		
		// TODO: getSource always returns GenericSource, because of the type of provider (LyricsProvider)
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
        	
        	public void handleError() {
        		mHandler.handleError();
        		useNextProvider();
        	}
        });
	}
	
	public String getResponse() {
		return mResponse;
	}
}
