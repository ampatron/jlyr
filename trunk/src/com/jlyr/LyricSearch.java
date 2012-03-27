package com.jlyr;

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
	
	public static final String TAG = "JLyrSearch"; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        mTitle = (EditText) findViewById(R.id.title_txt);
        mArtist = (EditText) findViewById(R.id.artist_txt);
        mBtn = (Button) findViewById(R.id.search_btn);
        
        mBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String title = (String) mTitle.getText().toString();
            	String artist = (String) mArtist.getText().toString();
            	
            	Intent intent = new Intent(LyricSearch.this, LyricViewer.class);
            	intent.putExtra("Track.title", title);
        		intent.putExtra("Track.artist", artist);
            	startActivity(intent);
            }
        });
    }
}