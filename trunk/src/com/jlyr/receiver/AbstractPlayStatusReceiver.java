package com.jlyr.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jlyr.util.NowPlaying;
import com.jlyr.LyricService;
import com.jlyr.util.Track;

public abstract class AbstractPlayStatusReceiver extends BroadcastReceiver {

	private static final String TAG = "JLyrPlayStatusReceiver";
	
	private Intent mService = null;
	protected Track mTrack = null;
	protected Track.State mState = null;

	@Override
	public final void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Bundle bundle = intent.getExtras();

		Log.v(TAG, "Action received was: " + action);

		// check to make sure we actually got something
		if (action == null || bundle == null) {
			Log.w(TAG, "Got null action or null bundle");
			return;
		}

		mService = new Intent(LyricService.ACTION_PLAYSTATECHANGED);
		
		try {
			parseIntent(context, action, bundle); // might throw

			// parseIntent must have called setMusicAPI and setTrack
			// with non-null values
			if (mTrack == null) {
				throw new IllegalArgumentException("null track");
			}

			NowPlaying np = new NowPlaying();
			np.addItem(mTrack, mState);

			// start/call the LyricService
			context.startService(mService);
		} catch (IllegalArgumentException e) {
			Log.i(TAG, "Got a bad track, ignoring it (" + e.getMessage() + ")");
		}
	}

	/**
	 * Sets the {@link Track.State} that this received broadcast represents.
	 * 
	 * @param state
	 */
	protected final void setState(Track.State state) {
		mState = state;
	}

	/**
	 * Sets the {@link Track} for this scrobble request
	 * 
	 * @param track
	 *            the Track for this scrobble request
	 */
	protected final void setTrack(Track track) {
		mTrack = track;
	}
	
	/**
	 * Parses the API / music app specific parts of the received broadcast. This
	 * is extracted into a specific {@link MusicAPI}, {@link Track} and state.
	 * 
	 * @see #setMusicAPI(MusicAPI)
	 * @see #setState(com.adam.aslfms.util.Track.State)
	 * @see #setTrack(Track)
	 * 
	 * @param ctx
	 *            to be able to create {@code MusicAPIs}
	 * @param action
	 *            the action/intent used for this scrobble request
	 * @param bundle
	 *            the data sent with this request
	 * @throws IllegalArgumentException
	 *             when the data received is invalid
	 */
	protected abstract void parseIntent(Context ctx, String action,
			Bundle bundle) throws IllegalArgumentException;

}
