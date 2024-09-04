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
	
	default String reference(ViewDecorator vd) { //JSON
		return identity();
	}
	
	default JDBCType type(ViewDecorator vd) {
		return null; // auto type
	}
	
	default ColumnBuilder builder(ViewDecorator vd) { //set type if null
		return null; // no builder by default
	}
	
	default CriteriaBuilder<ComparisonExpression> criteria(String name) {
		return null; // no criteria by default
	}

//	default JDBCArgumentParser parser(ViewDecorator vd) // override parser | format | local | validation
	
	default String pattern(ViewDecorator td) {
		throw new UnsupportedOperationException(); //improve API security and performance
	}

	default boolean canSelect(ViewDecorator td) {
		throw new UnsupportedOperationException(); //authorization inject
	}

	default boolean canFilter(ViewDecorator td) {
		throw new UnsupportedOperationException(); //authorization inject
	}
}
