package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface QueryExecutor<T> {

	T execute(QueryComposer query, Store ds);
	
	static <T> QueryExecutor<T> defaultExecutor(ResultSetMapper<T> mapper){
		return (q,s)-> s.execute(q.compose(s), mapper);
	}
}
