package org.usf.jquery.web;

import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.Validation.requireLegalAlias;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBComparator;
import org.usf.jquery.core.QueryParameterBuilder;
import org.usf.jquery.core.TableColumn;
import org.usf.jquery.core.TaggableColumn;

public interface ColumnDecorator extends TaggableColumn {
	
	String identity(); //URL
	
	@Override
	String reference(); //JSON

	@Override
	default String sql(QueryParameterBuilder builder) {
		var table  = (TableDecorator) requireNonNull(builder.getMainTable());
		var column = requireNonNull(column(table));
		return column.sql(builder);
	}
	
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
