package org.usf.jquery.web;

import static org.usf.jquery.web.ModelIterator.iterator;

import java.util.Map;

import org.usf.jquery.core.RequestQueryBuilder;

/**
 * 
 * @author u$f
 *
 */
public interface IterableViewDecorator<T> extends ViewDecorator {

	void tableName(String name, T ctx);
	
	T[] parseIterable(Map<String, String[]> parameterMap);
	
	@Override
	default void parseFilters(RequestQueryBuilder query, Map<String, String[]> parameters) {
		query.repeat(iterator(parseIterable(parameters)));
		ViewDecorator.super.parseFilters(query, parameters);
	}
}
