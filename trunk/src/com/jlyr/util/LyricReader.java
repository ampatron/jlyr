package com.jlyr.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.Environment;
import android.util.Log;

import com.jlyr.util.Track;

public class LyricReader {
	
	Track mTrack = null;
	File mFile = null;
	
	public static final String TAG = "JLyrReader";
	
	public LyricReader(Track track) {
		mTrack = track;
		getLyricsFileFromTrack();
	}
	
	public LyricReader(File file) {
		mFile = file;
		getTrackFromLyricsFile();
	}
	
	public File getFile() {
		return mFile;
	}
	
	public Track getTrack() {
		return mTrack;
	}
	
	public static File getLyricsDirectory() {
		File path = Environment.getExternalStorageDirectory(); 
        File file = new File(path + "/JLyr");
        
        return file;
	}
	
	private void getLyricsFileFromTrack() {
		if (mTrack == null) {
			Log.e(TAG, "Track is null. Cannot get lyrics file.");
			return;
		} else if (mFile != null) {
			Log.e(TAG, "File is not null. Cannot get lyrics file.");
			return;
		}
		File path = Environment.getExternalStorageDirectory();
		String filename = md5(mTrack.getArtist() + " - " + mTrack.getTitle()); 
        mFile = new File(path, "JLyr/" + filename + ".txt");
	}
	
	private void getTrackFromLyricsFile() {
		if (mFile == null) {
			Log.e(TAG, "File is null. Cannot get track.");
			return;
		} else if (mTrack != null) {
			Log.e(TAG, "Track is not null. Cannot get track.");
			return;
		}
        String trackInfo = null;
        
        if (mFile.exists()) {
        	Log.i(TAG, "Lyrics found on disk");
        	String eol = System.getProperty("line.separator");
        	try {
        		FileInputStream in = new FileInputStream(mFile);
    			BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
    			String aDataRow = "";
    			String aBuffer = "";
    			while ((aDataRow = inReader.readLine()) != null && aDataRow != "") {
    				aBuffer += aDataRow + eol;
    			}
    			trackInfo = aBuffer;
    			inReader.close();
        	} catch (IOException e) {
        		Log.e(TAG, "Could not read saved file.");
        	}
        }
		
		if (trackInfo != null) {
			String eol = System.getProperty("line.separator");
			String[] infoArray = trackInfo.split(eol);
			String artist = null;
			String title = null;
			String album = null;
			for (int i=0; i<infoArray.length; i++) {
				if (infoArray[i].startsWith("artist: ")) {
					artist = infoArray[i].substring(8);
				} else if (infoArray[i].startsWith("title: ")) {
					title = infoArray[i].substring(7);
				} else if (infoArray[i].startsWith("album: ")) {
					album = infoArray[i].substring(7);
				}
			}
			mTrack = new Track(artist, title, album, null);
		}
	}
	
	public String[] getContent() {
        String lyrics = null;
        String trackInfo = null;
        
        if (mFile.exists()) {
        	Log.i(TAG, "Lyrics found on disk");
        	String eol = System.getProperty("line.separator");
        	try {
        		FileInputStream in = new FileInputStream(mFile);
    			BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
    			String aDataRow = "";
    			String aBuffer = "";
    			while ((aDataRow = inReader.readLine()) != null) {
    				aBuffer += aDataRow + eol;
    			}
    			String[] content = aBuffer.split(eol + eol, 2);
    			trackInfo = content[0];
    			lyrics = content[1];
    			inReader.close();
        	} catch (IOException e) {
        		Log.e(TAG, "Could not read saved file.");
        	}
        }
		
		return new String[] {trackInfo, lyrics};
	}
	
	public void save(String response) {
        Log.i(TAG, "Saving lyrics to " + mFile.getAbsolutePath());
        if (mFile.canWrite()) {
        	Log.w(TAG, "Cannot write to file");
        	return;
        }
        String eol = System.getProperty("line.separator");
        String trackInfo = "title: " + mTrack.getTitle() + eol + 
        				   "artist: " + mTrack.getArtist() + eol + 
        				   "album: " + mTrack.getAlbum() + eol;
        try {
        	mFile.getParentFile().mkdirs();
        	mFile.createNewFile();
        	
        	FileOutputStream out = new FileOutputStream(mFile.getAbsolutePath());
        	OutputStreamWriter outWriter = new OutputStreamWriter(out);
        	outWriter.append(trackInfo + eol);
        	outWriter.append(response);
        	outWriter.close();
            out.close();
        } catch (IOException e) {
        	Log.w(TAG, "Error writing to file", e);
        } 
	}
	
	public static String md5(String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	        
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        return hexString.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
}
