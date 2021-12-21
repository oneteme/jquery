package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.mapNullableOrEmpty;

public interface DBTable extends DBObject<String> {
	
	String getTableName();
	
	DBColumn[] getColumns();
	
	DBFilter[] getClauses();
	
	String getColumnName(DBColumn column);
	
	DBColumn getRevisionColumn();
	
	@Override
	default String toSql(String schema) {
		return mapNullableOrEmpty(schema, v-> v+".") + getTableName();
	}
	
	//partition table
	default String toSql(String schema, int year) {
		return toSql(schema) + "_" + year;
	}
	
}
