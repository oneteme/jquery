package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface QueryExecutor<T> {

	T execute(Query query);
	
	public static <T> QueryExecutor<T> defaultExecutor(ResultSetMapper<T> mapper){
		return q-> q.execute(mapper);
	}
}
