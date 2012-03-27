package com.jlyr.util;

import android.util.Log;

public class Lyrics {
	Track mTrack = null;
	LyricReader mReader = null;
	LyricFetcher mFetcher = null;
	String mLyrics = null;
	GenericHandler mLyrHandler = null;
	
	public static final String TAG = "JLyrLyrics";
	
	public Lyrics(Track track) {
		mTrack = track;
		mReader = new LyricReader(mTrack);
		mFetcher = new LyricFetcher(mTrack);
		mLyrics = null;
	}
	
	public void loadLyrics() {
		loadLyrics(new GenericHandler());
	}
	
	public void loadLyrics(GenericHandler lyr_handler) {
		String[] content = mReader.getContent();
		mLyrics = content[1];
		mLyrHandler = lyr_handler;
		
		if (mLyrics == null) {
	    	Log.i(TAG, "Lyrics not found on disk. Fetching...");
	        mFetcher.fetchLyrics(new GenericHandler() {
	        	public void handleSuccess() {
	        		String lyrics = mFetcher.getResponse();
	        		if (lyrics != null) {
	    	        	mReader.save(lyrics);
	    	        	String[] content = mReader.getContent();
	    	    		mLyrics = content[1];
	    	        } else {
	    	        	mLyrics = null;
	    	        }
	        		mLyrHandler.handleSuccess();
	        	}
	        	
	        	public void handleError() {
	        		mLyrHandler.handleError();
	        	}
	        });
		} else {
			mLyrHandler.handleSuccess();
		}
	}
	
	public String getLyrics() {
		return mLyrics;
	}
}
