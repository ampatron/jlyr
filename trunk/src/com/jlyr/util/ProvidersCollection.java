package com.jlyr.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.jlyr.providers.ChartLyricsProvider;
import com.jlyr.providers.DummyProvider;
import com.jlyr.providers.LyrDbProvider;
import com.jlyr.providers.LyricsProvider;

public class ProvidersCollection {
	private static final LinkedHashMap<String, Class<?>> map = new LinkedHashMap<String, Class<?>>() {
		private static final long serialVersionUID = 1L;

		{
			put("Dummy", DummyProvider.class);
			put("LyrDB", LyrDbProvider.class);
			put("ChartLyrics", ChartLyricsProvider.class);
		}
	};
	
	private List<String> mSources = null;
	
	public static final String TAG = "JLyrProvidersCollection";
	
	public ProvidersCollection(String[] sources) {
		if (sources != null) {
			mSources = Arrays.asList(sources);
		} else {
			mSources = Arrays.asList(map.keySet().toArray(new String[0]));
		}
	}
	
	public LyricsProvider providerFromClass(Class<?> cl, Track track) {
		Constructor<?> ctor = null;
		LyricsProvider provider = null;
		try {
			ctor = cl.getDeclaredConstructor(Track.class);
			ctor.setAccessible(true);
			provider = (LyricsProvider) ctor.newInstance(track);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return provider;
	}
	
	public LyricsProvider[] toArray(Track track) {
		ArrayList<LyricsProvider> providers = new ArrayList<LyricsProvider>();
		for (String source : mSources) {
			if (!map.containsKey(source)) {
				Log.w(TAG, "Skipping unknown provider: " + source);
				continue;
			}
			Class<?> cl = map.get(source);
			LyricsProvider provider = providerFromClass(cl, track);
			if (provider == null) {
				Log.e(TAG, "Error loading provider for " + source);
				continue;
			} else {
				providers.add(provider);
			}
		}
		return providers.toArray(new LyricsProvider[0]);
	}
}
