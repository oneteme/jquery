package org.usf.jdbc.jquery.web;

import static org.usf.jdbc.jquery.ComparisonSingleExpression.equal;
import static org.usf.jdbc.jquery.ComparisonSingleExpression.in;

import org.usf.jdbc.jquery.ComparatorExpression;
import org.usf.jdbc.jquery.TableColumn;
import org.usf.jdbc.jquery.TaggableColumn;

public interface ColumnDescriptor {
	
	String name(); //URL
	
	String value(); //JSON
	
	default TaggableColumn from(TableDescriptor table) {
		var cn = table.columnName(this);
		return cn == null ? null : new TableColumn(cn, value());
	}
	
	default ComparatorExpression expression(ColumnMetadata cm, String... values) {
		return values.length == 1 
				? equal(cm.parseArg(values[0])) 
				: in(cm.parseArgs(values));
	}
}
