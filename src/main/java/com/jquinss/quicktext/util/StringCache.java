package com.jquinss.quicktext.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class StringCache extends LinkedHashMap<String, String>  {

	private static final long serialVersionUID = -8146766540649976347L;
	private int maxItems;
	
	public StringCache(int maxItems) {
		if (maxItems <= 0) {
			throw new IllegalArgumentException("The number of items must be greater than 0");
		}
		this.maxItems = maxItems;
	}
	
	public void setMaxItems(int maxItems) {
		if (maxItems <= 0) {
			throw new IllegalArgumentException("The number of items must be greater than 0");
		}
		
		int excess = size() - maxItems;
		if (excess > 0) {
			removeEldestEntries(excess);
		}
		this.maxItems = maxItems;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
		return size() > maxItems;
	}
	
	private void removeEldestEntries(int numEntries) {
		Iterator<String> iterator = this.keySet().iterator();
		while (iterator.hasNext() & numEntries > 0) {
			iterator.next();
			iterator.remove();
			--numEntries;
		}
	}
}