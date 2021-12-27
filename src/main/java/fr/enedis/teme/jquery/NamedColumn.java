package fr.enedis.teme.jquery;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NamedColumn implements DBColumn {
	
	@NonNull
	private final String name;
	@NonNull
	private final DBColumn column;
	
	@Override
	public String sql(DBTable table, ParameterHolder arg) {
		return column.sql(table, arg);
	}
	
	@Override
	public String tag(DBTable table) {
		return name;
	}
	
	@Override
	public NamedColumn as(String name) { //map
		return new NamedColumn(name, this.column);
	}

}
