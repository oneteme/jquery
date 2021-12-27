package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;

public interface DBTable extends DBObject<String> {
	
	String getTableName();
	
	String getColumnName(DBColumn column);
	
	DBColumn getRevisionColumn();
	
	@Override
	default String sql(String schema, ParameterHolder ph) {
		return isBlank(schema) 
				? getTableName() 
				: schema + "." + getTableName();
	}
	
	//partition table
	default String toSql(String schema, int year, ParameterHolder ph) {
		return sql(schema, ph) + "_" + year;
	}
	
	static DBTable mockTable() {
		return new DBTable() {

			@Override
			public String getTableName() {
				return "${table}";
			}
			
			@Override
			public String getColumnName(DBColumn column) {
				return "${column}";
			}
			
			@Override
			public DBColumn getRevisionColumn() {
				return null;
			}
		}; 
	}
}
