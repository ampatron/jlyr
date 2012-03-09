package com.jlyr.util;

import android.util.Log;

public class Lyrics {
	Track mTrack = null;
	LyricReader mReader = null;
	LyricFetcher mFetcher = null;
	String mLyrics = null;
	
	public static final String TAG = "JLyrLyrics";
	
	public Lyrics(Track track) {
		mTrack = track;
		mReader = new LyricReader(mTrack);
		mFetcher = new LyricFetcher(mTrack);
		mLyrics = null;
	}
	
	public void loadLyrics() {
		String[] content = mReader.getContent();
		String trackInfo = content[0];
		mLyrics = content[1];
		
		if (mLyrics == null) {
	    	Log.i(TAG, "Lyrics not found on disk. Fetching...");
	        mLyrics = mFetcher.fetchLyrics();
	        
	        if (mLyrics != null) {
	        	mReader.save(mLyrics);
	        }
		}
	}
	
	public String getLyrics() {
		return mLyrics;
	}
}
