package org.usf.jquery.web;

import static java.util.Objects.requireNonNull;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.QueryParameterBuilder;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;

public interface ColumnDecorator extends TaggableColumn {
	
	String identity(); //URL
	
	@Override
	String reference(); //JSON
	
	default TaggableColumn column(TableDecorator table) {
		var id = requireNonNull(table.columnName(this));
		return new TableColumn(id, reference(), table.reference());
	}
	
	default DBFilter filter(TableDecorator table, TableMetadata meta, String... values) {
    	var column = requireNonNull(column(table));
    	var parser = requireNonNull(parser(meta));
    	return values.length == 1 
    			? column.equal(parser.parseArg(values[0]))
    			: column.in(parser.parseArgs(values));
	}
	
	default ArgumentParser parser(TableMetadata metadata) {
		return metadata.column(this); //avoid metaMap.get(this) if overridden
	}
	
	@Override
	default String sql(QueryParameterBuilder builder) {
		var column = column((TableDecorator) builder.getMainTable());
		return column.sql(builder);
	}
}
