package org.usf.jquery.core;

import javax.sql.DataSource;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface QueryExecutor<T> {

	T execute(Query query, DataSource ds);
}
