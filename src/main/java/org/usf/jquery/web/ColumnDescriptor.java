package org.usf.jquery.web;

import static org.usf.jquery.core.ComparisonSingleExpression.equal;
import static org.usf.jquery.core.ComparisonSingleExpression.in;

import org.usf.jquery.core.ComparatorExpression;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;

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
