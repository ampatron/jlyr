package com.jlyr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import com.jlyr.util.InternalTrackTransmitter;
import com.jlyr.util.GenericHandler;
import com.jlyr.util.Track;
import com.jlyr.util.Lyrics;

public class LyricViewer extends Activity {
	
	TextView mText = null;
	Track mTrack = null;
	Lyrics mLyrics = null;
	
	public static final String TAG = "JLyrViewer"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer);
        
        mText = (TextView) findViewById(R.id.text);
        mText.setMovementMethod(new ScrollingMovementMethod());
        
        fillLyrics();
    }
    
    private void fillLyrics() {
    	mTrack = getTrackFromIntent();
    	if (mTrack == null) {
    		mTrack = getPlayingTrack();
    		
    		if (mTrack == null) {
    			mText.setText(getText(R.string.no_track_specified));
    			return;
    		}
    	}
    	
    	String trackInfoStr = mTrack.getArtist() + " - " + mTrack.getTitle();
    	mText.setText("Loading lyrics for " + trackInfoStr + " ...");
    	
    	mLyrics = new Lyrics(mTrack);
        mLyrics.loadLyrics(new GenericHandler() {
        	public void handleSuccess() {
        		String trackInfoStr = mTrack.getArtist() + " - " + mTrack.getTitle();
                String lyricsStr = mLyrics.getLyrics();
                
                mText.setText(trackInfoStr + "\n" + ((lyricsStr == null)? getText(R.string.lyrics_not_found) : lyricsStr));
        	}
        	
        	public void handleError() {
        		Context context = getApplicationContext();
                CharSequence text = "An error occured!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
        	}
        });
    }
    
    private Track getTrackFromIntent() {
    	Intent i = getIntent();
    	
    	String title = i.getStringExtra("Track.title");
        String artist = i.getStringExtra("Track.artist");
        String album = i.getStringExtra("Track.album");
        String year = i.getStringExtra("Track.year");
        
        if (title == null && artist == null) {
        	return null;
        } else {
        	Track track = new Track(artist, title, album, year);
	        return track;
        }
    }
    
    private Track getPlayingTrack() {
    	Track lastTrack = InternalTrackTransmitter.getLastTrack();
        if (lastTrack == null) {
        	return null;
        } else {
        	return lastTrack;
        }
    }
}