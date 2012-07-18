package com.jlyr.util;

import java.io.File;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TrackBrowser implements Runnable {

	public static final int DID_START = 0;
	public static final int DID_ERROR = 1;
	public static final int DID_SUCCEED = 2;
	public static final int ADD = 3;

	private Handler handler;
	
	public static final String TAG = "JLyrTrackBrowser";

	public class TrackView {
		private Track mTrack = null;
		
		public TrackView(Track track) {
			mTrack = track;
		}
		
		public Track getTrack() {
			return mTrack;
		}
		
		public String toString() {
			if (mTrack == null) {
				return "Track is null!";
			} else {
				return mTrack.toString();
			}
		}
	};
	
	public TrackBrowser() {
		this(new Handler());
	}

	public TrackBrowser(Handler _handler) {
		handler = _handler;
	}

	public void run() {
		handler.sendMessage(Message.obtain(handler, TrackBrowser.DID_START));
		try {
			File dir = LyricReader.getLyricsDirectory();
	        if (!dir.exists()) {
	        	dir.mkdirs();
	        }
	        File[] file_list = dir.listFiles();
	        
	        for (int i = 0; i < file_list.length; i++) {
	        	LyricReader reader = new LyricReader(file_list[i]);
	        	Track track = reader.getTrack();
	        	Log.i(TAG, "Add: " + track);
	        	TrackView tv = new TrackView(track);
	        	handler.sendMessage(Message.obtain(handler, TrackBrowser.ADD, tv));
	        }
	        handler.sendMessage(Message.obtain(handler, TrackBrowser.DID_SUCCEED));
		} catch (Exception e) {
			handler.sendMessage(Message.obtain(handler, TrackBrowser.DID_ERROR, e));
		}
	}
 }