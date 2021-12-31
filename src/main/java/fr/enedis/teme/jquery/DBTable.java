package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;

public interface DBTable extends DBObject<String> {
	
	String getTableName();
	
	String dbColumnName(DBColumn column);

	TableColumn[] columns();

	@Override
	default String sql(String schema, ParameterHolder ph) {
		return isBlank(schema) 
				? getTableName() 
				: schema + "." + getTableName();
	}

	default String sql(String schema, String suffix, ParameterHolder ph) {
		return isBlank(suffix) 
				? sql(schema, ph)
				: sql(schema, ph) + "_" + suffix;
	}
	
	static DBTable mockTable() {
		return new DBTable() {

			@Override
			public String getTableName() {
				return "${table}";
			}
			
			@Override
			public String dbColumnName(DBColumn column) {
				return column.getTag();
			}
			
			@Override
			public TableColumn[] columns() {
				throw new UnsupportedOperationException();
			}
		}; 
	}
}
