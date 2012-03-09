package com.jlyr.providers;

import com.jlyr.util.Track;

import android.util.Log;

public class LyrDbProvider extends LyricsProvider {
	
	String mSource = "LyrDB";
	
	public static final String TAG = "JLyrLyrDBProvider";
	
	public LyrDbProvider(Track track) {
		super(track);
	}
	
	@Override
	public String getLyrics() {
		String firstUrl = "http://webservices.lyrdb.com/lookup.php?q=" + 
					enc(mTrack.getArtist() + "|" + mTrack.getTitle()) + 
					"&for=match&agent=llyrics";
		String response = getUrl(firstUrl);
		
		if (response == null) {
			return null;
		}
		
		int end = response.indexOf("\\");
		if (end == -1) {
			return null;
		}
		String lyricsid = response.substring(0, end);
		Log.i(TAG, "LyrDB id is: " + lyricsid);
		String secondUrl = "http://www.lyrdb.com/getlyr.php?q=" + enc(lyricsid);
		
		String lyrics = getUrl(secondUrl);
		return lyrics;
	}
	
	public String toString() {
		return mSource;
	}
}
