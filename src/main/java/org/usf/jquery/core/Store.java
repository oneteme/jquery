package org.usf.jquery.core;

import javax.sql.DataSource;

/**
 * 
 * @author u$f
 *
 */
public interface Store {
	
	Dialect dialect();
	
	DataSource dataSource();
}
