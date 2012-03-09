package com.jlyr.util;

import java.util.LinkedList;

public class InternalTrackTransmitter {
	private static LinkedList<Track> tracks = new LinkedList<Track>();

	/**
	 * Appends {@code track} to the queue of tracks that
	 * {@link ScrobblingService} will pickup.
	 * <p>
	 * The method is thread-safe.
	 * 
	 * @see #popTrack()
	 * 
	 * @param track
	 *            the track to be appended
	 */
	public static synchronized void appendTrack(Track track) {
		tracks.addLast(track);
	}

	/**
	 * Pops a {@code Track} from the queue of tracks in FIFO order.
	 * <p>
	 * The method is thread-safe.
	 * 
	 * @see #appendTrack(Track)
	 * 
	 * @return the track at the front of the list
	 */
	public synchronized static Track popTrack() {
		if (tracks.isEmpty())
			return null;
		return tracks.removeFirst();
	}
	
	/**
	 * Returns last {@code Track} from the queue of tracks in FIFO order.
	 * <p>
	 * The method is thread-safe.
	 * 
	 * @see #appendTrack(Track)
	 * 
	 * @return the track at the front of the list
	 */
	public synchronized static Track getLastTrack() {
		if (tracks.isEmpty())
			return null;
		return tracks.getLast();
	}
}
