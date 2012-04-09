package com.jlyr;

import java.util.Arrays;
import java.util.List;

import com.jlyr.util.ProvidersCollection;

import cz.destil.settleup.gui.MultiSpinner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LyricSearch extends Activity {
	
	private EditText mTitle;
	private EditText mArtist;
	private Button mBtn;
	private MultiSpinner mSources;
	
	public static final String TAG = "JLyrSearch"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        mTitle = (EditText) findViewById(R.id.title_txt);
        mArtist = (EditText) findViewById(R.id.artist_txt);
        mSources = (MultiSpinner) findViewById(R.id.sources_spinner);
        mBtn = (Button) findViewById(R.id.search_btn);
        
        ProvidersCollection providerColl = new ProvidersCollection(null);
        String[] sources = (String[]) providerColl.getSources().toArray();
        
        mSources.setItems(Arrays.asList(sources), "All", new MultiSpinner.MultiSpinnerListener() {
			@Override
			public void onItemsSelected(boolean[] selected) {
			}
		});
        
        mBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String title = (String) mTitle.getText().toString();
            	String artist = (String) mArtist.getText().toString();
            	//String source = (String) mSource.getSelectedItem().toString();
            	//String[] sources = new String[] { source };
            	List<String> sourcesList = mSources.getSelectedItems();
            	String[] sources = sourcesList.toArray(new String[] {});
            	
            	Log.i(TAG, "Searching for: " + title + " - " + artist + " in: " + sources.toString());
            	
            	Intent intent = new Intent(LyricSearch.this, LyricViewer.class);
            	intent.putExtra("Track.title", title);
        		intent.putExtra("Track.artist", artist);
        		intent.putExtra("LyricsSources", sources);
            	startActivity(intent);
            }
        });
    }
}