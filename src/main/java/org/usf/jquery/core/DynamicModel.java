package org.usf.jquery.core;

import java.util.LinkedHashMap;

//TODO change to List<Entry<String, Object>>

@SuppressWarnings("serial")
public final class DynamicModel extends LinkedHashMap<String, Object> {
	
	@SuppressWarnings("unchecked")
	public <T> T getField(String name) {
		return (T) get(name);
	}
	
	public <T> T getField(String name, T defaultValue) {
		T v = getField(name);
		return v == null ? defaultValue : v;
	}
}
