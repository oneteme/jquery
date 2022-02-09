package fr.enedis.teme.jquery;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;

final class QueryColumnBuilder {

	@Getter
	private final String schema;
	private final Map<String, Entry<TaggableColumn, DBTable>> columns;
	
	public QueryColumnBuilder(String schema) {
		this.schema = schema;
		this.columns = new HashMap<>();
	}

	public boolean appendColumn(TaggableColumn column, DBTable table){
		if(!columns.containsKey(column.tagname())) {			
			columns.put(column.tagname(), Map.entry(column, table));
			return true;
		}
		return false;
	}
	
	public Entry<TaggableColumn, DBTable> getEntry(TaggableColumn column){
		return columns.get(column.tagname());
	}
	
	public Collection<Entry<TaggableColumn, DBTable>> entries(){
		return columns.values();
	}
	

	public String[] columns(){
		return columns.values().stream()
				.map(Entry::getKey)
				.map(TaggableColumn::tagname)
				.toArray(String[]::new);
	}
	
	public String columnsAsSelect(){
		return columns.values().stream()
				.map(e-> e.getValue().logicalColumnName(e.getKey()))
				.collect(joining(", "));
	}
}
