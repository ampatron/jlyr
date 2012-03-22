package com.jlyr.providers;

import com.jlyr.util.GenericHandler;
import com.jlyr.util.Track;

import edu.gvsu.masl.asynchttp.HttpConnection;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DummyProvider extends LyricsProvider {
	
	String mSource = "Dummy";
	
	public static final String TAG = "JLyrDummyProvider";
	
	public DummyProvider(Track track) {
		super(track);
	}
	
	@Override
	public void loadLyrics(GenericHandler _handler) {
		mHandler = _handler;
		
		mLyrics = null;
		mHandler.handleSuccess();
	}
	
	public String toString() {
		return mSource;
	}
}
