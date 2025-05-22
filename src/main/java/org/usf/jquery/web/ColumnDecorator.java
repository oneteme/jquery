package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.JQuery.currentEnvironment;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBColumn;
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
		var meta = currentEnvironment().getMetadata().columnMetadata(vd, this);
		return nonNull(meta) ? meta.getType() : null;
	}
	
	default Builder<ViewDecorator, DBColumn> builder() {
		return null; // no builder by default
	}
	
	default Builder<ViewDecorator, ComparisonExpression> criteria(String name) {
		return null; // no criteria by default
	}

	default JDBCArgumentParser parser(ViewDecorator vd) {
		throw new UnsupportedOperationException("not impl."); // override parser | format | local | validation
	}
	
	default String pattern(ViewDecorator td) {
		throw new UnsupportedOperationException("not impl."); //improve API security and performance
	}

	default boolean canSelect(ViewDecorator td) {
		throw new UnsupportedOperationException("not impl."); //authorization inject
	}

	default boolean canFilter(ViewDecorator td) {
		throw new UnsupportedOperationException("not impl."); //authorization inject
	}
}
