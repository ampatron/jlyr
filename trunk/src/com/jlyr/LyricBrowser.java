package com.jlyr;


import com.jlyr.util.Track;
import com.jlyr.util.TrackBrowser;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
 * TODO: sort the tracks in lyric browser. i think they are sorted by "last modified" or "created" date.
 * TODO: load the lyrics asynchronously (done, just see if its better to use android.os.Handler
 * TODO: add a menu in the LyricViewer so that lyrics can be deleted, reloaded or fetched from a specific provider
 * TODO: add a menu item in browser to wipe all downloaded lyrics
 * TODO: add a preference to save or not the lyrics
 * TODO: ScrobbleDroidReceiver handle SAME_AS_CURRENT tracks better. What if it's a null SAME_AS_CURRENT?
 * TODO: add a preference to choose lyrics providers (even a single one)
 * TODO: LyrDbProvider fix the error that occurs when lyrics are not found (i think its fixed now)
 * TODO: add more providers
 * TODO: add more receivers
 * TODO: allow to choose receivers (like in Simple Last.fm Scrobbler)
 * TODO: add preference to fetch lyrics on wifi only, etc.
 * TODO: add preference to allow automatically downloading lyrics (without pressing on the notification or Now Playing)
 */

public class LyricBrowser extends ListActivity {
	
	private Menu mMenu;
	private ArrayAdapter<TrackBrowser.TrackView> la = null;
	
	public static final String TAG = "JLyrBrowser"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        populateList();
    }
    
    private void populateList() {
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        la = new ArrayAdapter<TrackBrowser.TrackView>(this, R.layout.list_item);
        setListAdapter(la);
        
        Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case TrackBrowser.DID_START: {
					Log.i(TAG, "Getting stored lyrics list...");
					break;
				}
				case TrackBrowser.DID_SUCCEED: {
					Log.i(TAG, "Done.");
					break;
				}
				case TrackBrowser.ADD: {
					TrackBrowser.TrackView tv = (TrackBrowser.TrackView) message.obj;
					la.add(tv);
					break;
				}
				case TrackBrowser.DID_ERROR: {
					Exception e = (Exception) message.obj;
					// TODO: try e.toString() maybe it gives more detail about the error
					// Otherwise find a way to use printStackTrace()
					Log.e(TAG, "Error: " + e.getMessage());
					break;
				}
				}
			}
		};
        
        TrackBrowser tb = new TrackBrowser(handler); 
        Thread thread = new Thread(tb);
		thread.start();
        
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	TrackBrowser.TrackView tv = (TrackBrowser.TrackView) parent.getAdapter().getItem(position);
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
