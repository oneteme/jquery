package org.usf.jquery.core;

import java.util.LinkedHashMap;

/**
 * 
 * @author u$f
 *
 */
//change to List<Entry<String, Object>>
@SuppressWarnings("serial")
public final class DynamicModel extends LinkedHashMap<String, Object> {
	
	@SuppressWarnings("unchecked")
	public <T> T getField(String name) {
		return (T) get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getField(String name, T defaultValue) {
		return (T) getOrDefault(name, defaultValue);
	}
}
