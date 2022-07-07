package fr.enedis.teme.jquery;

import java.util.stream.Stream;

import fr.enedis.teme.jquery.web.ColumnDescriptor;

public interface DBTable extends DBObject {
	
	String dbName();

	String dbColumnName(ColumnDescriptor desc);
	
	default TableColumn get(ColumnDescriptor desc) {
		var colomnName = dbColumnName(desc);
		if(colomnName == null) {
			throw new IllegalArgumentException(desc + " was not declared in " + this);
		}
		return new TableColumn(colomnName, desc.value());
	}
	
	@Override
	default String sql(QueryParameterBuilder ph) {
		return dbName();
	}

	default TableAdapter suffix(String suffix) {
		return new TableAdapter(this, suffix);
	}

	default RequestQuery select(ColumnDescriptor...columns){
		return new RequestQuery().select(this)
				.columns(columns != null, ()-> 
					Stream.of(columns)
					.map(this::get)
					.toArray(TableColumn[]::new));
	}

}
