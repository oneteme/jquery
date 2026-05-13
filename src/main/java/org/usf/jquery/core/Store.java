package org.usf.jquery.core;

import javax.sql.DataSource;

/**
 * 
 * @author u$f
 *
 */
public interface Store {
	
	String name();

	Dialect dialect();
	
	DataSource dataSource();
	
	default QueryComposer newQueryComposer() {
		return new QueryComposer(this);
	}
	
	default QueryView newQueryView() {
		return new QueryView(this);
	}
}
