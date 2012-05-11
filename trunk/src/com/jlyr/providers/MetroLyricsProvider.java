package com.jlyr.providers;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.jlyr.util.Track;

import edu.gvsu.masl.asynchttp.HttpConnection;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MetroLyricsProvider extends LyricsProvider {
	
	public static final String TAG = "JLyrMetroLyricsProvider";
	
	public MetroLyricsProvider(Track track) {
		super(track);
	}
	
	public String getSource() {
		return "MetroLyrics";
	}
	
	@Override
	public void loadLyrics(Handler _handler) {
		mHandler = _handler;
		
		String punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
		String clean_artist = mTrack.getArtist();
		String clean_title = mTrack.getTitle();
		for (int i=0; i<punctuation.length(); i++) {
			char c = punctuation.charAt(i);
			clean_title = clean_title.replace(String.valueOf(c), "");
			clean_artist = clean_artist.replace(String.valueOf(c), "");
		}
		clean_title = clean_title.replace(" ", "-");
		clean_artist = clean_artist.replace(" ", "-");
		
		final String baseURL = "http://www.metrolyrics.com/" + enc(clean_title) + "-lyrics-" + enc(clean_artist) + ".html";
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case HttpConnection.DID_START: {
					Log.i(TAG, "Getting lyrics...");
					break;
				}
				case HttpConnection.DID_SUCCEED: {
					String response = (String) message.obj;
					mLyrics = parse(response, baseURL);
					break;
				}
				case HttpConnection.DID_ERROR: {
					Exception e = (Exception) message.obj;
					// TODO: try e.toString() maybe it gives more detail about the error
					// Otherwise find a way to use printStackTrace()
					Log.e(TAG, "Error: " + e.getMessage());
					
					mLyrics = null;
					
					doError();
					break;
				}
				}
			}
		};
		new HttpConnection(handler).get(baseURL);
		Log.v(TAG, "Fetching url: " + baseURL);
	}
	
	private String parse(String response, String url) {
		Document doc = Parser.parse(response, url);
		
		Element title_el = doc.select("html > head > title").first();
		String title = null;
		if (title_el == null) {
			Log.w(TAG, "No title tag");
		} else {
			title = title_el.text();
			int end = title.indexOf(" LYRICS");
			title = title.substring(0, end);
		}
		
		Elements els = doc.select("div#lyrics-body > p > span, div#lyrics-body > p > br");

		String eol = System.getProperty("line.separator");
		String lyrics = "";
		for (Element el : els) {
			String tag = el.tagName();
			if (tag == "br") {
				lyrics += eol;
			} else if (tag == "span") {
				if (el.children().size() == 0) {
					lyrics += el.text() + eol;
				}
			}
		}
		
		/*
        int start_title = response.indexOf("<title>");
        if (start_title == -1) {
        	Log.e(TAG, "Title tag was not found");
        	doFail();
            return null;
        }
        String title = response.substring(start_title);
        
        int end_title = title.indexOf(" LYRICS</title>");
        if (end_title == -1) {
        	Log.e(TAG, "Title closing tag was not found");
        	doFail();
            return null;
        }
        title = title.substring(0, end_title);
        //title = URLDecoder.decode(title);
        Log.v(TAG, "Page title:" + title);
        
        int start_lyrics = response.indexOf("<span class='line line-s' id='line_1'>");
        if (start_lyrics == -1) {
        	Log.e(TAG, "Lyrics tag was not found");
        	doFail();
            return null;
        }
        String lyrics = response.substring(start_lyrics);
        
        int end_lyrics = lyrics.indexOf("</div>");
        if (end_lyrics == -1) {
        	Log.e(TAG, "Lyrics closing tag was not found");
        	doFail();
            return null;
        }
        lyrics = lyrics.substring(0, end_lyrics);
        
        lyrics = lyrics.replace("<span class='line line-s' id='line_1'>", "");
        lyrics = lyrics.replaceAll("\\<span class\\=\\'line line-s\\' id\\=\\'line_[0-9][0-9]?\\'\\>\\<span style\\=\\'color:#888888;font-size:0\\.75em\\'\\>\\[.+\\]\\</span\\>", "");
        lyrics = lyrics.replaceAll("\\<meta [^\\>]*\\>", "");
        lyrics = lyrics.replaceAll("\\<span class\\=\\'line line-s\\' id\\=\\'line_[0-9][0-9]?\\'\\>", "&#10;");
        lyrics = lyrics.replaceAll("\\<em class\\=\\\"smline sm\\\" data-meaningid\\=\\\"[0-9]+\\\" \\>", "");
        lyrics = lyrics.replaceAll("(\\</em\\>)?\\</span\\>", "");
        lyrics = lyrics.replaceAll("(\\<br /\\>)*\\</p\\>", "");
        lyrics = lyrics.replace("<br />", "");
        lyrics = lyrics.replace("&#", "");
        lyrics = lyrics.trim();
        lyrics = lyrics.substring(0, lyrics.length()-1);
        
        String[] chars = lyrics.split(";");
        String decoded_lyrics = "";
        for (String c : chars) {
        	int i = (int) Integer.valueOf(c);
        	decoded_lyrics += String.valueOf((char) i);
        }
        lyrics = decoded_lyrics;
        */

		doLoad();
		return "[ MetroLyrics - " + (title==null? "NULL":title) + " ]" + eol + lyrics;
	}
}
