package com.jlyr;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jlyr.util.LyricReader;
import com.jlyr.util.Lyrics;
import com.jlyr.util.NowPlaying;
import com.jlyr.util.Track;

public class LyricService extends Service {

	private static final String TAG = "JLyrService";

	public static final String ACTION_PLAYSTATECHANGED = "com.jlyr.playstatechanged";
	private static final int NOTIFICATION_ID = 1536;

	private Track mCurrentTrack = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent i, int startId) {
		if (i == null) {
			Log.e(TAG, "Intent is null");
			return;
		}
		String action = i.getAction();
		
		if (action.equals(ACTION_PLAYSTATECHANGED)) {
			NowPlaying np = new NowPlaying();
			mCurrentTrack = np.getTrack();
			
			Log.i(TAG, "Hello: " + mCurrentTrack);
			
			if (mCurrentTrack == null) {
				clearNotification();
			} else {
				showNotification();
			}
		} else {
			Log.e(TAG, "Weird action in onStart: " + action);
		}
	}
	
	private boolean useAutoFetch() {
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		 
		boolean auto_fetch = SP.getBoolean("auto_fetch_lyrics", false);
		
		if (!auto_fetch) {
			Log.i(TAG, "Auto-fetch is off. Will not fetch lyrics.");
			return false;
		}
		 
		boolean wifi_only = SP.getBoolean("fetch_wifi_only", false);
		
		if (!wifi_only) {
			return true;
		}
		
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
			return true;
		} else {
			Log.i(TAG, "Wifi is off. Will not fetch lyrics.");
			return false;
		}
	}
	
	private boolean useNotification() {
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		 
		String notif = SP.getString("notifications", "0");
		
		if (notif.equals("0")) {
			// Never show
			return false;
		} else if (notif.equals("1")) {
			// Always show
			return true;
		} else if (notif.equals("2")) {
			// Show only if saved
			LyricReader lr = new LyricReader(mCurrentTrack);
			File file = lr.getFile();
			if (file == null || !file.exists()) {
				Log.i(TAG, "File " + file + " does not exist.");
				return false;
			}
		} else if (notif.equals("3")) {
			// Show only if not saved
			LyricReader lr = new LyricReader(mCurrentTrack);
			File file = lr.getFile();
			if (file != null && file.exists()) {
				Log.i(TAG, "File " + file + " exists already.");
				return false;
			}
		}
		return false;
	}
	
	private Handler getLoadHandler(final Lyrics lyrics) {
    	Handler handler = new Handler() {
			public void handleMessage(Message message) {
				switch (message.what) {
				case Lyrics.DID_TRY:
					break;
				case Lyrics.DID_LOAD:
					if (useNotification()) {
						doShowNotification();
					} else {
						clearNotification();
					}
					break;
				case Lyrics.DID_FAIL:
				case Lyrics.DID_ERROR:
					if (useNotification()) {
						doShowNotification();
					} else {
						clearNotification();
					}
					break;
				case Lyrics.IS_TRYING:
					break;
				}
			}
		};
    	return handler;
    }
	
	private void showNotification() {
		// Handle auto-fetching lyrics
		if (useAutoFetch()) {
			Lyrics lyrics = new Lyrics(getBaseContext(), mCurrentTrack, (String[]) null, true);
			lyrics.loadLyrics(getLoadHandler(lyrics));
		} else if (useNotification()) {
			doShowNotification();
		} else {
			clearNotification();
		}
	}
	
	private void doShowNotification() {
		// Show the notification regardless of settings and lyrics fetched or saved or not
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Lyrics Available";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = getText(R.string.notification_title);
		CharSequence contentText = mCurrentTrack.toString();
		
		Intent notificationIntent = new Intent(this, LyricViewer.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	private void clearNotification() {
		// Clear the notification regardless of its existence, no problem in clearing notifications if it does not
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}