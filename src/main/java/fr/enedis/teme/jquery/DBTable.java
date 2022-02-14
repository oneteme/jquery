package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.SqlStringBuilder.POINT_SEPARATOR;
import static fr.enedis.teme.jquery.Utils.isBlank;

public interface DBTable extends DBObject<String> {
	
	String physicalName();
	
	String physicalColumnName(TableColumn column);

	TableColumn[] columns();
	
	default TableAdapter suffix(String suffix) {
		return new TableAdapter(this, suffix);
	}

	@Override
	default String sql(String schema, QueryParameterBuilder ph) {
		
		return isBlank(schema) 
				? physicalName() 
				: schema + POINT_SEPARATOR + physicalName();
	}
	
	default RequestQuery selectAll(){
		return new RequestQuery().select(this, columns());
	}

	default RequestQuery select(TaggableColumn...columns){
		return new RequestQuery().select(this, columns);
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
