package fr.enedis.teme.jquery;

import static java.util.Optional.ofNullable;

public interface DBTable extends DBObject<String> {
	
	String getTableName();
	
	DBColumn[] getColumns();
	
	DBFilter[] getClauses();
	
	String getColumnName(DBColumn column);
	
	@Override
	default String toSql(String schema) {
		return ofNullable(schema).map(v-> v+".").orElse("") + getTableName();
	}
	
	//partition table
	default String toSql(String schema, Integer year) {
		return toSql(schema);// + "_" + year; //TODO : wait table create
	}
	
}
