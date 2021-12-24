package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;

import lombok.NonNull;

public interface DBTable extends DBObject<String> {
	
	String getTableName();
	
	String getColumnName(@NonNull DBColumn column);
	
	DBColumn getRevisionColumn();
	
	@Override
	default String toSql(String schema) {
		return isBlank(schema) 
				? getTableName() 
				: schema + "." + getTableName();
	}
	
	//partition table
	default String toSql(String schema, int year) {
		return toSql(schema) + "_" + year;
	}
	
}
