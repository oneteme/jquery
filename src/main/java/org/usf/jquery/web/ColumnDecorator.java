package org.usf.jquery.web;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.JDBCType;

/**
 * 
 * @author u$f
 * 
 *
 */
@FunctionalInterface
public interface ColumnDecorator {
	
	String identity();  //URL unique
	
	default String reference(ViewDecorator td) { //JSON
		return identity();
	}
	
	default JDBCType type(ViewDecorator td) {
		return null;
	}
	
	default JDBCArgumentParser parser(ViewDecorator td){ // override parser | format | local | validation
		return null; // jdbcArgParser(dataType(td))
	}
	
	default ColumnBuilder builder(ViewDecorator td) { //set type if null
		return null; // no builder by default
	}
	
	default CriteriaBuilder<ComparisonExpression> criteria(String name) {
		return null; // no criteria by default
	}
	
	default String pattern(ViewDecorator td) {
		throw new UnsupportedOperationException(); //improve API security and performance
	}

	default boolean canSelect(ViewDecorator td) {
		throw new UnsupportedOperationException(); //authorization inject
	}

	default boolean canFilter(ViewDecorator td) {
		throw new UnsupportedOperationException(); //authorization inject
	}
	
	static ColumnDecorator ofColumn(String ref, ColumnBuilder cb) {
		return new ColumnDecorator() {
			@Override
			public String identity() {
				return ref; // default column tag
			}
			
			@Override
			public ColumnBuilder builder(ViewDecorator td) {
				return cb;
			}
		};
	}
}
