package com.jlyr;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jlyr.util.InternalTrackTransmitter;
import com.jlyr.util.LyricReader;
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
		Bundle extras = i.getExtras();
		
		if (action.equals(ACTION_PLAYSTATECHANGED)) {
			if (extras == null) {
				Log.e(TAG, "Got null extras on playstatechange");
				return;
			}
			Track.State state = Track.State.valueOf(extras.getString("state"));

			Track track = InternalTrackTransmitter.getLastTrack();

			if (track == null) {
				Log.e(TAG, "A null track got through!! (Ignoring it)");
				return;
			}

			onPlayStateChanged(track, state);
		} else {
			Log.e(TAG, "Weird action in onStart: " + action);
		}
	}

	private synchronized void onPlayStateChanged(Track track, Track.State state) {
		Log.d(TAG, "State: " + state.name());
		if (track.equals(Track.SAME_AS_CURRENT)) {
			// this only happens for apps implementing Scrobble Droid's API
			Log.d(TAG, "Got a SAME_AS_CURRENT track");
			if (mCurrentTrack != null) {
				track = mCurrentTrack;
			} else {
				Log.e(TAG, "Got a SAME_AS_CURRENT track, but current was null!");
				return;
			}
		}

		if (state == Track.State.START || state == Track.State.RESUME) { // start/resume
			if (mCurrentTrack != null) {
				// Clear previous notification
				clearNotification();
			}

			mCurrentTrack = track;
			// Show notification
			showNotification();
		} else if (state == Track.State.PAUSE) { // pause
			if (mCurrentTrack == null) {
				// just ignore the track
			} else {
				if (!track.equals(mCurrentTrack)) {
					Log.e(TAG, "PStopped track doesn't equal currentTrack!");
					Log.e(TAG, "t: " + track);
					Log.e(TAG, "c: " + mCurrentTrack);
				} else {
					// Do nothing
				}
			}
		} else if (state == Track.State.COMPLETE) { // "complete"
			// TODO test this state
			if (mCurrentTrack == null) {
				// just ignore the track
			} else {
				if (!track.equals(mCurrentTrack)) {
					Log.e(TAG, "CStopped track doesn't equal currentTrack!");
					Log.e(TAG, "t: " + track);
					Log.e(TAG, "c: " + mCurrentTrack);
				} else {
					// Clear previous notification
					clearNotification();
					mCurrentTrack = null;
				}
			}
		} else if (state == Track.State.PLAYLIST_FINISHED) { // playlist end
			if (mCurrentTrack == null) {
				// Clear previous notification
				clearNotification();
			} else {
				if (!track.equals(mCurrentTrack)) {
					Log.e(TAG, "PFStopped track doesn't equal currentTrack!");
					Log.e(TAG, "t: " + track);
					Log.e(TAG, "c: " + mCurrentTrack);
				} else {
					// Clear previous notification
					clearNotification();
				}
			}

			mCurrentTrack = null;
		} else if (state == Track.State.UNKNOWN_NONPLAYING) {
			// similar to PAUSE, but might scrobble if close enough
			if (mCurrentTrack == null) {
				// just ignore the track
			} else {
				// Clear or ignore previous notification
				clearNotification();
			}
		} else {
			Log.e(TAG, "Unknown track state: " + state.toString());
		}
	}
	
	private String useNotification() {
		SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		 
		String notifications = SP.getString("notifications", "0");
		return notifications;
	}
	
	private void showNotification() {
		String notif = useNotification();
		if (notif.equals("0")) {
			return;
		} else if (notif.equals("2")) {
			LyricReader lr = new LyricReader(mCurrentTrack);
			File file = lr.getFile();
			if (file == null || !file.exists()) {
				Log.i(TAG, "File " + file + " does not exist.");
				return;
			}
		} else if (notif.equals("3")) {
			LyricReader lr = new LyricReader(mCurrentTrack);
			File file = lr.getFile();
			if (file != null && file.exists()) {
				Log.i(TAG, "File " + file + " exists already.");
				return;
			}
		}
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Lyrics Available";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = getText(R.string.notification_title);
		CharSequence contentText = mCurrentTrack.getArtist() + " - " + mCurrentTrack.getTitle();
		
		Intent notificationIntent = new Intent(this, LyricViewer.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	private void clearNotification() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}