package com.jlyr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
        String[] array = res.getStringArray(R.array.main_list);
        
        ArrayAdapter<String> la = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
        setListAdapter(la);
        
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
        	Intent intent = null;
        	switch (position) {
        	case 0:
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
