package org.usf.jquery.web;

import static java.util.Objects.requireNonNull;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;

public interface ColumnDecorator {
	
	String name(); //URL
	
	String value(); //JSON
	
	default TaggableColumn column(TableDecorator table) {
		var cn = requireNonNull(table.columnName(this));
		return new TableColumn(cn, value());
	}
	
	default DBFilter filter(TableDecorator table, TableMetadata tm, String... values) {
    	var column = requireNonNull(column(table));
    	var meta = requireNonNull(metadata(tm));
    	return values.length == 1 
    			? column.equal(meta.parseArg(values[0]))
    			: column.in(meta.parseArgs(values));
	}
	
	default ColumnMetadata metadata(TableMetadata metadata) {
		return metadata.column(this);
	}
}
