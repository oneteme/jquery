package org.usf.jquery.web;

import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.Validation.requireLegalAlias;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBComparator;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;

public interface ColumnDecorator  {
	
	String identity(); //URL
	
	String reference(); //JSON
	
	default TaggableColumn column(TableDecorator table) {
		var sql = requireLegalAlias(table.columnName(this));
		return new TableColumn(sql, reference(), table.reference());
	}
	
	default ComparisonExpression expression(TableMetadata meta, String... values) {
    	var parser = requireNonNull(parser(meta));
    	return values.length == 1
    			? DBComparator.equal(parser.parseArg(values[0]))
    			: DBComparator.in(parser.parseArgs(values));
	}
	
	default ArgumentParser parser(TableMetadata metadata) {
		return metadata.column(this); //avoid metaMap.get(this) if overridden
	}
}
