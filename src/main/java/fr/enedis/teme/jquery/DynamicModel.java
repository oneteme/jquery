package fr.enedis.teme.jquery;

import java.util.LinkedHashMap;

import lombok.NonNull;

@SuppressWarnings("serial")
public final class DynamicModel extends LinkedHashMap<String, Object> {

	@SuppressWarnings("unchecked")
	public <T> T getField(@NonNull DBColumn column) {
		return (T) get(column.getMappedName());
	}

	public void setField(@NonNull DBColumn fn, Object value){
		put(fn.getMappedName(), value);
	}

}
