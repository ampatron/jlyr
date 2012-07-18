package com.jlyr.providers;

import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.jlyr.util.Track;

import edu.gvsu.masl.asynchttp.HttpConnection;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AZLyricsProvider extends LyricsProvider {
	
	public static final String TAG = "JLyrAZLyricsProvider";
	
	public AZLyricsProvider(Track track) {
		super(track);
	}
	
	public String getSource() {
		return "AZLyrics";
	}
	
	@Override
	public void loadLyrics(Handler _handler) {
		mHandler = _handler;
		
		// TODO: we are relying on DuckDuckGo, we shouldn't:
		// AZLyrics removes spaces: /lyrics/jamesblunt/staythenight.html
		// and maybe punctuation also. See ILyrics they do it in Ruby, but I didn't get it right yet.
		String search_query = "\\ azlyrics " + mTrack.getArtist() + " " + mTrack.getTitle();
		final String baseURL = "http://www.duckduckgo.com/?q=" + enc(search_query);
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case HttpConnection.DID_START: {
					Log.i(TAG, "Getting lyrics link...");
					break;
				}
				case HttpConnection.DID_SUCCEED: {
					String response = (String) message.obj;
					Document doc = Parser.parse(response, baseURL);
					String content = doc.select("meta[http-equiv=refresh]").first().attr("content");
					String url = content.substring(content.indexOf("url=")+4);
					if (url.startsWith("http://www.azlyrics.com/lyrics/")) {
						getActualContent(url);
					} else {
						Log.w(TAG, "DuckDuckGo got a wrong link: " + url);
						doFail();
					}
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
	
	private void getActualContent(String url) {
		final String baseURL = url;
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
			title = title.replace(" LYRICS - ", " - ");
		}
		
		Elements divs = doc.select("div#main div");
		if (divs.size()<4) {
			Log.e(TAG, "No body div");
			doFail();
			return null;
		}
		Element body = divs.get(3);

		if (body == null) {
			Log.e(TAG, "No lyrics div tag");
			doFail();
			return null;
		}

		String eol = System.getProperty("line.separator");
		List<Node> els = body.childNodes();
		String lyrics = "";
		for (Node node : els) {
			if (node instanceof TextNode) {
				lyrics += ((TextNode) node).text();
			} else {
				lyrics += eol;
			}
		}

		doLoad();
		return "[ AZLyrics - " + (title==null? "NULL":title) + " ]" + eol + lyrics;
	}
}
