package com.jlyr.providers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jlyr.util.GenericHandler;
import com.jlyr.util.Track;

import edu.gvsu.masl.asynchttp.HttpConnection;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ChartLyricsProvider extends LyricsProvider {
	
	public static final String TAG = "JLyrChartLyricsProvider";
	
	public ChartLyricsProvider(Track track) {
		super(track);
	}
	
	public String getSource() {
		return "ChartLyrics";
	}
	
	@Override
	public void loadLyrics(GenericHandler _handler) {
		mHandler = _handler;
		
		String baseURL = "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?" + 
					"artist=" + enc(mTrack.getArtist()) + "&" + 
					"song=" + enc(mTrack.getTitle());
		Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case HttpConnection.DID_START: {
					Log.i(TAG, "Getting lyrics...");
					break;
				}
				case HttpConnection.DID_SUCCEED: {
					String response = (String) message.obj;
					mLyrics = parse(response);
					mHandler.handleSuccess();
					break;
				}
				case HttpConnection.DID_ERROR: {
					Exception e = (Exception) message.obj;
					// TODO: try e.toString() maybe it gives more detail about the error
					// Otherwise find a way to use printStackTrace()
					Log.e(TAG, "Error: " + e.getMessage());
					
					mLyrics = null;
					mHandler.handleError();
					break;
				}
				}
			}
		};
		new HttpConnection(handler).get(baseURL);
		Log.v(TAG, "Fetching url: " + baseURL);
	}
	
	private String parse(String response) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		Document dom = null;
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			InputStream is = null;
			try {
	            is = new ByteArrayInputStream(response.getBytes("UTF-8"));
	        } catch (UnsupportedEncodingException e) {
	        	Log.e(TAG, "String lacks support for UTF-8!?");
	        	return null;
	        }
			dom = db.parse(is);
		}catch(ParserConfigurationException pce) {
			Log.e(TAG, "Error parsing XML: ParserConfigurationException");
			return null;
		}catch(SAXException se) {
			Log.e(TAG, "Error parsing XML: SAXException");
			return null;
		}catch(IOException ioe) {
			Log.e(TAG, "Error parsing XML: IOException");
			return null;
		}
		
		Element docEle = dom.getDocumentElement();

		String artist = getFirstElementValue(docEle, "LyricArtist");
		String title = getFirstElementValue(docEle, "LyricSong");
		String lyrics = getFirstElementValue(docEle, "Lyric");

		if (lyrics == null) {
			Log.e(TAG, "No <Lyric> XML tag found");
			return null;
		} else {
			String eol = System.getProperty("line.separator");
			return "[ " + (artist==null? "NULL" : artist) + " - " + (title==null? "NULL" : title) + " ]" + eol + lyrics;
		}
	}

	private String getFirstElementValue(Element docEle, String element) {
		NodeList nl = docEle.getElementsByTagName(element);
		if(nl != null && nl.getLength() == 1) {
			Element el = (Element)nl.item(0);
			Node child = el.getFirstChild();
			if (child == null) {
				return null;
			}
			String content = child.getNodeValue();
			
			return content;
		} else {
			return null;
		}
	}
}
