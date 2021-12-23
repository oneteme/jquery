package fr.enedis.teme.jquery;

import static java.util.Optional.ofNullable;

import lombok.NonNull;

public interface DBTable extends DBObject<String> {
	
	String getTableName();
	
	DBColumn[] getColumns();
	
	DBFilter[] getClauses();
	
	String getColumnName(@NonNull DBColumn column);
	
	DBColumn getRevisionColumn();
	
	@Override
	default String toSql(String schema) {
		return ofNullable(schema)
				.map(v-> v+".")
				.orElse("") + getTableName();
	}
	
	//partition table
	default String toSql(String schema, int year) {
		return toSql(schema) + "_" + year;
	}
	
}
