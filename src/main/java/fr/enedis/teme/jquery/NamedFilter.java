package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.formatString;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NamedFilter implements DBFilter, Taggable<DBTable> {
	
	@NonNull
	private final String name;
	@NonNull
	private final DBFilter filter;
	
	@Override
	public String sql(DBTable table, ParameterHolder arg) {
		return "WHEN " + filter.sql(table, arg) +  " THEN " + formatString(name); //secure column name
	}
	
	@Override
	public String tag(DBTable table) {
		return name;
	}

	@Override
	public NamedFilter as(String name) { //map
		return new NamedFilter(name, this.filter);
	}

}
