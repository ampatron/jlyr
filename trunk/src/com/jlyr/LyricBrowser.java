package com.jlyr;

import java.io.File;

import com.jlyr.util.LyricReader;
import com.jlyr.util.Track;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

/*
 * TODO: change the main screen. it shouldn't be the browser. and change the launcher name
 * TODO: load the lyrics asynchronously
 * TODO: add a menu in the LyricViewer so that lyrics can be deleted, reloaded or fetched from a specific provider
 * TODO: add a menu item in browser to wipe all downloaded lyrics
 * TODO: add a preference to save or not the lyrics
 * TODO: ScrobbleDroidReceiver handle SAME_AS_CURRENT tracks better. What if it's a null SAME_AS_CURRENT?
 * TODO: add a preference to choose lyrics providers (even a single one)
 * TODO: LyrDbProvider fix the error that occurs when lyrics are not found
 * TODO: add more providers
 * TODO: add more receivers
 * TODO: allow to choose receivers (like in Simple Last.fm Scrobbler)
 * TODO: add preference to fetch lyrics on wifi only, etc.
 * TODO: add preference to allow automatically downloading lyrics (without pressing on the notification or Now Playing)
 */

public class LyricBrowser extends ListActivity {
	
	private Menu mMenu;
	
	public static final String TAG = "JLyrBrowser"; 
	
	class TrackView {
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
				return mTrack.getArtist() + " - " + mTrack.getTitle();
			}
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        populateList();
    }
    
    private void populateList() {
    	File dir = LyricReader.getLyricsDirectory();
        if (!dir.exists()) {
        	dir.mkdirs();
        }
        File[] file_list = dir.listFiles();
        
        TrackView[] array = new TrackView[file_list.length];
        for (int i = 0; i < file_list.length; i++) {
        	LyricReader reader = new LyricReader(file_list[i]);
        	Track track = reader.getTrack();
        	Log.i(TAG, "Add: " + track);
        	array[i] = new TrackView(track);
        }
        
        setListAdapter(new ArrayAdapter<TrackView>(this, R.layout.list_item, array));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	TrackView tv = (TrackView) parent.getAdapter().getItem(position);
        	Track track = tv.getTrack();
        	if (track != null) {
        		Intent intent = new Intent(LyricBrowser.this, LyricViewer.class);
        		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		intent.putExtra("Track.title", track.getTitle());
        		intent.putExtra("Track.artist", track.getArtist());
        		intent.putExtra("Track.album", track.getAlbum());
        		intent.putExtra("Track.year", track.getYear());
        		
        		startActivity(intent);
        		//finish();
        	}
          }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Hold on to this
        mMenu = menu;
        
        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browser, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // For "Title only": Examples of matching an ID with one assigned in
            //                   the XML
            case R.id.now_playing_menu_item:
            	Intent intent = new Intent(LyricBrowser.this, LyricViewer.class);
        		startActivity(intent);
        		//finish();
                return true;

            case R.id.reload_menu_item:
                //Toast.makeText(this, "Reloading lyrics list...", Toast.LENGTH_SHORT).show();
                populateList();
                return true;
            
            case R.id.settings_menu_item:
                Toast.makeText(this, "Settings!", Toast.LENGTH_SHORT).show();
                Intent settingsIntent = new Intent(LyricBrowser.this, JLyrSettings.class);
        		startActivity(settingsIntent);
        		//finish();
                return true;
                
            // Generic catch all for all the other menu resources
            default:
                // Don't toast text when a submenu is clicked
                if (!item.hasSubMenu()) {
                    Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
        }
        
        return false;
    }
    
    
}
