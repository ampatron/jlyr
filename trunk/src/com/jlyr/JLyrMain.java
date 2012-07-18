package com.jlyr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jlyr.util.Lyrics;
import com.jlyr.util.NowPlaying;
import com.jlyr.util.Track;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.util.Log;

public class JLyrMain extends ListActivity {
	
	public static final String TAG = "JLyrMain"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        populateList();
    }
    
    private void populateList() {
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        
        Resources res = getResources();
        String[] items = res.getStringArray(R.array.main_list);
        String[] details = res.getStringArray(R.array.main_list_details);
        
        NowPlaying np = new NowPlaying();
        Track track = np.getTrack();
        if (track != null) {
        	details[0] = track.toString();
        }
        
        NowPlaying.setHandler(new Handler() {
			public void handleMessage(Message message) {
				Log.i(TAG, "Got a message!");
			}
		});
        
        List<HashMap<String, String>> mArray = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i<items.length; i++) {
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put("item", items[i]);
        	map.put("details", details[i]);
        	mArray.add(map);
        }
        String[] from = {"item", "details"};
        int[] to = {android.R.id.text1, android.R.id.text2};

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		SimpleAdapter adapter = new SimpleAdapter(this.getApplicationContext(), mArray, android.R.layout.simple_list_item_2, from, to);
		lv.setAdapter(adapter);
		
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	Intent intent = null;
        	switch (position) {
        	case 0:
        		NowPlaying np = new NowPlaying();
                Track track = np.getTrack();
                if (track == null) {
                	break;
                }
        		intent = new Intent(JLyrMain.this, LyricViewer.class);
        		break;
        	case 1:
        		intent = new Intent(JLyrMain.this, LyricSearch.class);
        		break;
        	case 2:
        		intent = new Intent(JLyrMain.this, LyricBrowser.class);
        		break;
        	case 3:
        		intent = new Intent(JLyrMain.this, JLyrSettings.class);
        		break;
        	case 4:
        		showAboutDialog();
        		break;
        	default:
        		String item = (String) parent.getAdapter().getItem(position);
        		Log.e(TAG, "Got an undefined list item " + item + " at position " + position);
        		break;
        	}
        	if (intent != null) {
        		startActivity(intent);
        	}
          }
        });
    }
    
    private void showAboutDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this); 
    	builder.setTitle(R.string.app_name)
    		.setMessage(R.string.about)
	        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	dialog.cancel();
	            }
	        });
    	AlertDialog dialog = builder.create();
    	dialog.show();
    }
}
