package com.jlyr.providers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import com.jlyr.util.Track;

public class LyricsProvider {
	
	String mSource = "GenericSource";
	Track mTrack = null;
	
	public static final String TAG = "JLyrProvider";
	
	public LyricsProvider(Track track) {
		mTrack = track;
	}
	
	public String getSource() {
		return mSource;
	}
	
	public String getLyrics() {
		return null;
	}

	protected String getUrl(String uri) {
		String response = null;
		
		Log.d(TAG, "Get URI (provider: " + this.toString() + "): " + uri);
		Log.d(TAG, "for: " + mTrack.getTitle() + " - " + mTrack.getArtist());
		
		if (uri == null) {
			return null;
		}
		
		DefaultHttpClient http = new DefaultHttpClient();
		HttpGet request = new HttpGet(uri);
		
		try {
			ResponseHandler<String> handler = new BasicResponseHandler();
			response = http.execute(request, handler);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Error - ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			// TODO: try e.toString() maybe it gives more detail about the error
			// Otherwise find a way to use printStackTrace()
			Log.e(TAG, "Error - IOException: " + e.getMessage());
		} finally {
			http.getConnectionManager().shutdown();
		}
        
		return response;
	}
	
	protected static String enc(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "URLEncoder lacks support for UTF-8!?");
			return null;
		}
	}
}
