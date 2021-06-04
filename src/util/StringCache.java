package util;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringCache extends LinkedHashMap<String, String>  {

	private static final long serialVersionUID = -8146766540649976347L;
	private int maxItems;
	
	public StringCache(int maxItems) {
		this.maxItems = maxItems;
	}
	
	public void setMaxItems(int maxItems) {
		this.maxItems = maxItems;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
		return size() > maxItems;
	}
}
