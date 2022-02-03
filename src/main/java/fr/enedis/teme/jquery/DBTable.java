package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

public interface DBTable extends DBObject<String> {
	
	String physicalName();
	
	String physicalColumnName(TableColumn column);

	TableColumn[] columns();

	@Override
	default String sql(String schema, ParameterHolder ph) {
		return isBlank(schema) 
				? physicalName() 
				: schema + "." + physicalName();
	}

	default String sql(String schema, String suffix, ParameterHolder ph) {
		
		return new SqlStringBuilder(sql(schema, ph))
				.appendIf(!isBlank(suffix), ()-> "_" + suffix)
				.toString();
	}
	
	default List<TableColumn> joinColumns(DBTable table) {
		var cols = asList(columns());
		return Stream.of(table.columns())
				.filter(cols::contains)
				.collect(toList());
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
