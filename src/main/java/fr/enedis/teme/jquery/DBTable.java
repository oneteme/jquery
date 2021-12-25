package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;

public interface DBTable extends DBObject<String> {
	
	String getTableName();
	
	String getColumnName(DBColumn column);
	
	DBColumn getRevisionColumn();
	
	@Override
	default String sql(String schema) {
		return isBlank(schema) 
				? getTableName() 
				: schema + "." + getTableName();
	}

	@Override //tag not used here
	default String tag(String table) {
		return null;
	}
	
	//partition table
	default String toSql(String schema, int year) {
		return sql(schema) + "_" + year;
	}
}
