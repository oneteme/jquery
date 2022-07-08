package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.ComparisonSingleExpression.equal;
import static fr.enedis.teme.jquery.ComparisonSingleExpression.in;

import fr.enedis.teme.jquery.ComparatorExpression;
import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.TaggableColumn;

public interface ColumnDescriptor {
	
	String key();
	
	String value();
	
	default TaggableColumn column(DBTable table) {
		return table.get(this);
	}
	
	default ComparatorExpression expression(ColumnMetadata cm, String... values) {
		return values.length == 1 
				? equal(cm.parseArg(values[0])) 
				: in(cm.parseArgs(values));
	}
}
