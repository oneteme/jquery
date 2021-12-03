package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public final class DynamicModel extends LinkedHashMap<String, Object> {

	@SuppressWarnings("unchecked")
	public <T> T getField(DBColumn column) {
		
		return (T) get(requireNonNull(column).getMappedName());
	}

	public void setField(DBColumn fn, Object value){
		
		put(requireNonNull(fn).getMappedName(), value);
	}

}
