package com.jlyr;


import java.io.File;
import java.util.Comparator;

import com.jlyr.util.LyricReader;
import com.jlyr.util.Track;
import com.jlyr.util.TrackBrowser;
import com.jlyr.util.TrackBrowser.TrackView;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.util.Log;

/*
 * TODO: add a preference to save or not the lyrics
 * TODO: ScrobbleDroidReceiver handle SAME_AS_CURRENT tracks better. What if it's a null SAME_AS_CURRENT?
 * TODO: add a preference to choose lyrics providers (even a single one)
 * TODO: add more providers
 * TODO: add more receivers
 * TODO: allow to choose receivers (like in Simple Last.fm Scrobbler)
 * TODO: add preference to fetch lyrics on wifi only, etc.
 * TODO: add preference to allow automatically downloading lyrics (without pressing on the notification or Now Playing)
 * TODO: add option to choose to load from specific provider on long press from browser
 * TODO: main screen should display the name of now playing song
 * TODO: maybe use database to save names of stored lyrics, to use search and/or load better in browser
 * TODO: save in local file track info and provider/source. it should be displayed in the viewer
 * TODO: add edit functionality to the saved lyrics.
 * TODO: add canRead() and canSave() methods to providers, and provide the user with a choice to save there and there and read there and there. LyricReader will be a provider.
 * TODO: in browser use @string/... for long click choice dialog, same for delete,source in viewer menu
 * TODO: use @color instead of @integer for colors?
 */

public class LyricBrowser extends ListActivity {
	
	private Menu mMenu;
	private ArrayAdapter<TrackBrowser.TrackView> la = null;
	
	private static final Comparator<TrackBrowser.TrackView> mComparator = new Comparator<TrackBrowser.TrackView>() {

		@Override
		public int compare(TrackView arg0, TrackView arg1) {
			String str0 = arg0.toString();
			String str1 = arg1.toString();
			return str0.compareTo(str1);
		}
		
	};
	
	private static final String[] dialogItems = new String[] {"View", "Delete", "Reload"};
	
	public static final String TAG = "JLyrBrowser"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        populateList();
    }
    
    private void populateList() {
    	if (mMenu != null) {
	    	MenuItem mi = mMenu.getItem(0);
	    	mi.setEnabled(false);
    	}
    	
        final ListView lv = getListView();
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
					if (mMenu != null) {
				    	MenuItem mi = mMenu.getItem(0);
				    	mi.setEnabled(true);
			    	}
					la.sort(mComparator);
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
        		doView(track);
        	}
          }
        });
        
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                    int position, long id) {
              	TrackBrowser.TrackView tv = (TrackBrowser.TrackView) parent.getAdapter().getItem(position);
              	Track track = tv.getTrack();
              	if (track != null) {
              		showChoiceDialog(tv);
              	}
              	return true;
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
            
            case R.id.wipe_menu_item:
          		File dir = LyricReader.getLyricsDirectory();
          		for (File f : dir.listFiles()) {
          			f.delete();
          		}
          		populateList();
            	return true;
                
            default:
        		Log.e(TAG, "Got an undefined list item " + item.getTitle());
        		break;
        }
        
        return false;
    }

    public void showChoiceDialog(TrackBrowser.TrackView tv) {
    	final Track track = tv.getTrack();
    	AlertDialog.Builder builder = new AlertDialog.Builder(this); 
    	builder.setTitle(tv.toString())
    		   .setItems(dialogItems, new DialogInterface.OnClickListener() {
    			   public void onClick(DialogInterface dialog, int item) {
    				   switch (item) {
    				   case 0:
    					   doView(track);
    					   break;
    				   case 1:
    					   doDelete(track);
    					   populateList();
    					   break;
    				   case 2:
    					   doDelete(track);
    					   doView(track);
    					   break;
    				   default:
    					   Log.w(TAG, "Unknown item selected: " + item);
    					   break;
    				   }
    			   }
    		   });
    	AlertDialog dialog = builder.create();
    	dialog.show();
    }
    
    public void doView(Track track) {
    	Intent intent = new Intent(LyricBrowser.this, LyricViewer.class);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("Track.title", track.getTitle());
		intent.putExtra("Track.artist", track.getArtist());
		intent.putExtra("Track.album", track.getAlbum());
		intent.putExtra("Track.year", track.getYear());
		
		startActivity(intent);
		//finish();
    }
    
    public void doDelete(Track track) {
    	LyricReader reader = new LyricReader(track);
  		File file = reader.getFile();
  		file.delete();
    }
    
}
