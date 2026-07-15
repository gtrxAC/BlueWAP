package com.gtrxac.discord;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.lang.Math;

public class Util {
	// Remove least recently used CachedImage elements from hashtable
	// until there are less than "limit" elements.
	public static void hashtableCheckLimit(Hashtable ht, int limit) {
		synchronized (ht) {
			while (ht.size() >= limit) {
				int lowestLastUsed = Integer.MAX_VALUE;
				Object keyWithLowest = null;

				Enumeration keys = ht.keys();
				while (keys.hasMoreElements()) {
					Object thisKey = keys.nextElement();
					int thisLastUsed = ((CachedImage) ht.get(thisKey)).lastUsed;
					
					if (thisLastUsed < lowestLastUsed) {
						lowestLastUsed = thisLastUsed;
						keyWithLowest = thisKey;
					}
				}
				ht.remove(keyWithLowest);
			}
		}
	}

	public static void hashtablePutCachedImageWithLimit(Hashtable ht, Object key, CachedImage value, int limit) {
		hashtableCheckLimit(ht, limit);
		ht.put(key, value);
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (Exception e) {}
	}
}