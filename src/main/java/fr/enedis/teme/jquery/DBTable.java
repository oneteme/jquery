package fr.enedis.teme.jquery;

public interface DBTable extends DBObject<String> {
	
	String physicalName();
	
	String physicalColumnName(TableColumn column);

	TableColumn[] columns();

	@Override
	default String sql(String schema, QueryParameterBuilder ph) {
		
		return schema == null 
				? physicalName() 
				: schema + "." + physicalName();
	}
	
	static DBTable mockTable() {
		return new DBTable() {

			@Override
			public String physicalName() {
				return "${table}";
			}
			
			@Override
			public String physicalColumnName(TableColumn column) {
				return column.tagname();
			}
			
			@Override
			public TableColumn[] columns() {
				throw new UnsupportedOperationException();
			}
		}; 
	}
}
