package com.jlyr;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import com.jlyr.util.ProvidersCollection;

import cz.destil.settleup.gui.MultiSpinner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class LyricSearch extends Activity {
	
	private EditText mTitle;
	private EditText mArtist;
	
	private MultiSpinner mSources;
	private Button mBtn;
	
	private Spinner mSearchEngine;
	private Button mSearchBrowserBtn;
	
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
        
        mSearchEngine = (Spinner) findViewById(R.id.search_engine_spinner);
        mSearchBrowserBtn = (Button) findViewById(R.id.search_browser_btn);
        
        ProvidersCollection providerColl = new ProvidersCollection(getBaseContext(), null);
        String[] sources = (String[]) providerColl.getSources().toArray();
        
        mSources.setItems(Arrays.asList(sources), getString(R.string.all), new MultiSpinner.MultiSpinnerListener() {
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
            	
            	String sources_string = "";
            	for (String src : sources) {
            		sources_string += src + ", ";
            	}
            	if (sources_string.length() > 2) {
            		sources_string = sources_string.substring(0, sources_string.length() - 2);
            	}
            	
            	Log.i(TAG, "Searching for: " + title + " - " + artist + " in: " + sources_string);
            	
            	Intent intent = new Intent(LyricSearch.this, LyricViewer.class);
            	intent.putExtra("Track.title", title);
        		intent.putExtra("Track.artist", artist);
        		intent.putExtra("LyricsSources", sources);
            	startActivity(intent);
            }
        });
        
        mSearchBrowserBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	String title = (String) mTitle.getText().toString();
            	String artist = (String) mArtist.getText().toString();
            	String searchEngine = (String) mSearchEngine.getSelectedItem();
            	
            	Log.i(TAG, "Searching for: " + title + " - " + artist + " on " + searchEngine);
            	
            	String search_query = artist + " - " + title + " lyrics";
            	if (searchEngine.equals("DuckDuckGo")) {
            		search_query = artist + " - " + title + " lyrics";
            	} else if (searchEngine.equals("Google")) {
            		search_query = "!g " + artist + " - " + title + " lyrics";
            	} else if (searchEngine.equals("Bing")) {
            		search_query = "!b " + artist + " - " + title + " lyrics";
            	} else if (searchEngine.equals("Yahoo")) {
            		search_query = "!y " + artist + " - " + title + " lyrics";
            	}
        		String URL = "http://www.duckduckgo.com/?q=" + enc(search_query);
            	
        		Log.i(TAG, "The URL is: " + URL);
        		
            	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
            	startActivity(intent);
            }
        });
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