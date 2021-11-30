package fr.enedis.teme.jquery;

import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public final class DynamicModel extends LinkedHashMap<String, Object> {

	@SuppressWarnings("unchecked")
	public <T> T getField(Column column) {
		
		return (T) get(column.getMappedName());
	}

	public void setField(Column fn, Object value){
		
		put(fn.getMappedName(), value);
	}

}
