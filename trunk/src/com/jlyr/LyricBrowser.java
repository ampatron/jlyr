package com.jlyr;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

public class LyricBrowser extends ListActivity {
	
	private Menu mMenu;
	private ArrayAdapter<TrackBrowser.TrackView> la = null;
	private List<TrackBrowser.TrackView> mList = null;
	
	private static final Comparator<TrackBrowser.TrackView> mComparator = new Comparator<TrackBrowser.TrackView>() {

		@Override
		public int compare(TrackView arg0, TrackView arg1) {
			String str0 = arg0.toString();
			String str1 = arg1.toString();
			return str0.compareTo(str1);
		}
		
	};
	
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
        
        mList = new ArrayList<TrackBrowser.TrackView>();
        
        la = new ArrayAdapter<TrackBrowser.TrackView>(this, R.layout.list_item, mList);
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
					//la.sort(mComparator);
					break;
				}
				case TrackBrowser.ADD: {
					TrackBrowser.TrackView tv = (TrackBrowser.TrackView) message.obj;
					//la.add(tv);
					int index = Collections.binarySearch(mList, tv, mComparator);
					mList.add((index < 0) ? (-index - 1) : index, tv);
					la.notifyDataSetChanged();
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
    	
    	String[] dialogItems = new String[] {
    			getString(R.string.view),
    			getString(R.string.delete),
    			getString(R.string.reload)
    	};
    	
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
