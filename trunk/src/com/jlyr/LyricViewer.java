package com.jlyr;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import com.jlyr.util.LyricReader;
import com.jlyr.util.LyricsWebSearch;
import com.jlyr.util.NowPlaying;
import com.jlyr.util.ProvidersCollection;
import com.jlyr.util.Track;
import com.jlyr.util.Lyrics;

public class LyricViewer extends Activity {
	
	TextView mText = null;
	Menu mMenu = null;
	
	Track mTrack = null;
	Lyrics mLyrics = null;
	boolean isLoading = false;
	Remember mRemember = null;
	
	String[] mSources = null;
	String[] mAllSources = null;
	boolean[] mSelectedSources = null;
	
	int mSearchEngine = 0; 
	
	int mScrollY = 0;
	
	public static final String TAG = "JLyrViewer"; 
	
	private class Remember {
		public Track track = null;
		public Lyrics lyrics = null;
		public boolean loading = false;
		public String[] sources = null;
		public int scrollY = 0;
		public Handler handler;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewer);
        
        Remember r = (Remember) getLastNonConfigurationInstance();
        
        mText = (TextView) findViewById(R.id.text);
        mText.setMovementMethod(new ScrollingMovementMethod());
        
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        int bg_color = SP.getInt("viewer_bg_color", -1);
        if (bg_color != -1) {
        	mText.setBackgroundColor(bg_color);
        }
        
        int font_color = SP.getInt("viewer_font_color", -1);
        if (font_color != -1) {
        	mText.setTextColor(font_color);
        }
        
        String size_str = SP.getString("viewer_font_size", "-1");
        int size = -1;
        try {
        	size = Integer.valueOf(size_str);
        } catch (NumberFormatException e) {
        	size = -1;
        }
        if (size != -1) {
        	mText.setTextSize(size);
        }
        
        if (r != null) {
            mTrack = r.track;
            mLyrics = r.lyrics;
            mSources = r.sources;
            isLoading = r.loading;
            
            mScrollY = r.scrollY;
            
            if (mTrack == null) {
    			mText.setText(getText(R.string.no_track_specified));
    			return;
    		}
            
            if (isLoading) {
	            String trackInfoStr = mTrack.toString();
	        	mText.setText("Loading lyrics for " + trackInfoStr + " ...");
	        	r.handler = getLoadHandler();
            } else {
            	setLyrics();
            }
        } else {
        	fillLyrics();
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        mRemember = new Remember() {
        	{
        		track = mTrack;
        		lyrics = mLyrics;
        		loading = isLoading;
        		sources = mSources;
        		scrollY = mText.getScrollY();
        	}
        	
        };
        return mRemember;
    }
    
    private void fillLyrics() {
		if (mTrack == null) {
    		mTrack = getTrackFromIntent();
        	if (mTrack == null) {
        		mTrack = getPlayingTrack();
        		
        		if (mTrack == null) {
        			mText.setText(getText(R.string.no_track_specified));
        			return;
        		}
        	}
		}
    	
    	String trackInfoStr = mTrack.toString();
    	mText.setText("Loading lyrics for " + trackInfoStr + " ...");
    	
    	if (mSources == null) {
    		mSources = getSourcesFromIntent();
    	}
    	
    	loadLyrics();
    }
    
    private void loadLyrics() {
    	if (mMenu != null) {
    		mMenu.setGroupEnabled(0, false);
    	}
    	
    	mLyrics = new Lyrics(getBaseContext(), mTrack, mSources);
    	
    	isLoading = true;
    	mLyrics.loadLyrics(getLoadHandler());
    }
    
    private Handler getLoadHandler() {
    	Handler handler = new Handler() {
			public void handleMessage(Message message) {
				if (mRemember != null) {
					mRemember.loading = false;
					Message msg = Message.obtain(mRemember.handler, message.what, message.obj);
        			mRemember.handler.sendMessage(msg);
        			return;
				}
				switch (message.what) {
				case Lyrics.DID_TRY:
					Toast.makeText(getApplicationContext(), (((String) message.obj)) + " failed!", Toast.LENGTH_SHORT).show();
					break;
				case Lyrics.DID_LOAD:
	        		setLyrics();
					break;
				case Lyrics.DID_FAIL:
        			Toast.makeText(getApplicationContext(), "Lyrics not found!", Toast.LENGTH_SHORT).show();
        			setLyrics();
					break;
				case Lyrics.DID_ERROR:
        			Toast.makeText(getApplicationContext(), "An error occured!", Toast.LENGTH_SHORT).show();
        			setLyrics();
					break;
				case Lyrics.IS_TRYING:
        			Toast.makeText(getApplicationContext(), "Trying " + ((String) message.obj), Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};
    	return handler;
    }
    
    private void setLyrics() {
    	if (mMenu != null) {
	    	mMenu.setGroupEnabled(0, true);
    	}
    	
    	isLoading = false;
    	String trackInfoStr = mTrack.toString();
        String lyricsStr = mLyrics.getLyrics();
        
        mText.setText(trackInfoStr + "\n" + ((lyricsStr == null)? getText(R.string.lyrics_not_found) : lyricsStr));
        
        mText.scrollTo(0, mScrollY);
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
    
    private String[] getSourcesFromIntent() {
    	Intent i = getIntent();
    	
    	String [] sources = i.getStringArrayExtra("LyricsSources");
        
        return sources;
    }
    
    private Track getPlayingTrack() {
    	NowPlaying np = new NowPlaying();
    	return np.getTrack();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Hold on to this
        mMenu = menu;
        
        // Inflate the currently selected menu XML resource.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.viewer, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reload_menu_item:
                fillLyrics();
                return true;
            
            case R.id.save_menu_item:
                mLyrics.saveLyrics();
                return true;
            
            case R.id.delete_menu_item:
            	doDelete();
            	finish();
            	return true;
            
            case R.id.source_menu_item:
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	
            	ProvidersCollection providerColl = new ProvidersCollection(getBaseContext(), null);
                mAllSources = (String[]) providerColl.getSources().toArray();
            	
                mSelectedSources = new boolean[mAllSources.length];
                if (mSources == null) {
                	for (int i = 0; i < mSelectedSources.length; i++) {
                        mSelectedSources[i] = true;
                    }
                } else {
                	List<String> sourcesList = Arrays.asList(mSources);
                	for (int i = 0; i < mSelectedSources.length; i++) {
                        mSelectedSources[i] = sourcesList.contains(mAllSources[i]);
                    }
                }
                
                builder.setMultiChoiceItems(mAllSources, mSelectedSources, new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						mSelectedSources[which] = isChecked;
					}
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	ArrayList<String> sourcesList = new ArrayList<String>();
						for (int i = 0; i < mSelectedSources.length; i++) {
		                    if (mSelectedSources[i]) {
		                    	sourcesList.add(mAllSources[i]);
		                    }
						}
						mSources = sourcesList.toArray(new String[] {});
						doDelete();
						fillLyrics();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            	return true;
                
            case R.id.browser_menu_item:
            	AlertDialog.Builder searchBuilder = new AlertDialog.Builder(this);
                
            	Resources res = getResources();
            	final String[] searchEngines = res.getStringArray(R.array.search_engines);
            	
            	searchBuilder.setSingleChoiceItems(searchEngines, mSearchEngine, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mSearchEngine = which;
					}
            		
            	})
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	String searchEngine = searchEngines[mSearchEngine];
                    	
                    	Log.i(TAG, "Searching for: " + mTrack.toString() + " on " + searchEngine);
                    	
                    	LyricsWebSearch lws = new LyricsWebSearch(LyricViewer.this, mTrack, searchEngine);
                    	lws.start();
                    }
                });

                AlertDialog searchDialog = searchBuilder.create();
                searchDialog.show();
            	return true;
            	
            case R.id.info_menu_item:
            	LyricReader reader = new LyricReader(mTrack);
          		String[] content = reader.getContent();
            	String info;
            	if (content[0] == null) {
            		info = reader.getInfo();
            	} else {
            		info = content[0];
            	}
            	AlertDialog.Builder infoBuilder = new AlertDialog.Builder(this); 
            	infoBuilder.setTitle(mTrack.toString())
            		.setMessage(info)
        	        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	            public void onClick(DialogInterface dialog, int whichButton) {
        	            	dialog.cancel();
        	            }
        	        });
            	AlertDialog infoDialog = infoBuilder.create();
            	infoDialog.show();
            	return true;
            	
            default:
        		Log.e(TAG, "Got an undefined list item " + item.getTitle());
        		break;
        }
        
        return false;
    }
    
    private void doDelete() {
    	LyricReader reader = new LyricReader(mTrack);
  		File file = reader.getFile();
  		if (file.exists()) {
  			file.delete();
  		}
    }
}