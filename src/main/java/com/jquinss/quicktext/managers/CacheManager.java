package com.jquinss.quicktext.managers;

import com.jquinss.quicktext.util.StringCache;

public class CacheManager {
	private final StringCache cache;
	
	public CacheManager(StringCache cache) {
		this.cache = cache;
	}
	
	public void addToCache(String key, String value) {
		cache.put(key, value);
	}
	
	public void updateCache(String key, String value) {
		cache.replace(key, value);
	}
	
	public String getFromCache(String key) {
		return cache.get(key);
	}
	
	public void removeFromCache(String key) {
		cache.remove(key);
	}
	
	public void clearCache() {
		cache.clear();
	}
	
	public boolean isInCache(String key) {
		return cache.containsKey(key);
	}
	
	public void setCacheMaxItems(int maxItems) {
		cache.setMaxItems(maxItems);
	}
}
