package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBTable extends TaggableView {
	
	@Override
	default String sql(QueryParameterBuilder builder) {
		return tablename();
	}
	
	@Override 
	default String reference() {
		return tablename();
	}

	String tablename();
}
