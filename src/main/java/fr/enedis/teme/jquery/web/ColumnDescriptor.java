package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.ComparisonSingleExpression.equal;
import static fr.enedis.teme.jquery.ComparisonSingleExpression.in;

import fr.enedis.teme.jquery.ComparatorExpression;
import fr.enedis.teme.jquery.TableColumn;
import fr.enedis.teme.jquery.TaggableColumn;

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
