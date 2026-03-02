package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.web.JQuery.currentEnvironment;

import org.usf.jquery.core.Predicate;
import org.usf.jquery.core.Column;
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
	
	default Builder<ViewDecorator, Column> builder() {
		return null; // no builder by default
	}
	
	default Builder<ViewDecorator, Predicate> criteriaBuilder(String name) {
		return null; // no criteria by default
	}
	
	default Predicate criteria(String name, ViewDecorator vd, String... args) {
		return requireNonNull(criteriaBuilder(name), "criteriaBuilder")
				.build(vd, args);
	}
}
