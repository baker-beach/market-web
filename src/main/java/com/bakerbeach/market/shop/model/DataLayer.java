package com.bakerbeach.market.shop.model;

import java.util.HashMap;
import java.util.Map;

public class DataLayer {
	private Map<String, Object> sessionBasedMap = new HashMap<>();
	private Map<String, Object> requestBasedMap = new HashMap<>();

	public DataLayer(Map<String, Object> sessionBasedMap, Map<String, Object> requestBasedMap) {
		this.sessionBasedMap = sessionBasedMap;
		this.requestBasedMap = requestBasedMap;
	}
	
	public Object get(Object key) {
		if (requestBasedMap.containsKey(key)) {
			return requestBasedMap.get(key);
		}

		return sessionBasedMap.get(key);
	}

	public boolean containsKey(Object key) {
		if (requestBasedMap.containsKey(key)) {
			return true;
		}
		
		return sessionBasedMap.containsKey(key);
	}

	public Object put(String key, Object value, Boolean isRequestBased) {
		if (isRequestBased) {
			return requestBasedMap.put(key, value);
		} else {
			return sessionBasedMap.put(key, value);
		}
	}

}
