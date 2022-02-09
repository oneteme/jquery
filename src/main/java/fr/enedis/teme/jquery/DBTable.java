package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;

public interface DBTable extends DBObject<String> {
	
	String physicalName();
	
	String physicalColumnName(TableColumn column);

	TableColumn[] columns();

	@Override
	default String sql(String schema, QueryParameterBuilder ph) {
		return sql(schema, null, ph);
	}

	default String sql(String schema, String suffix, QueryParameterBuilder ph) {
		
		return new SqlStringBuilder(20)
				.appendIf(!isBlank(schema), ()-> schema + ".")
				.append(physicalName())
				.appendIf(!isBlank(suffix), ()-> "_" + suffix)
				.toString();
	}
	
	default String logicalColumnName(TaggableColumn column) {
		return column.tagname();
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
