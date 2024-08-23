package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.member;
import static org.usf.jquery.web.ModelIterator.iterator;

import java.util.Collection;
import java.util.Map;

import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.QueryParameterBuilder;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TableView;

/**
 * 
 * @author u$f
 *
 */
public interface IterableViewDecorator<T> extends ViewDecorator {
	
	@Override
	default ViewBuilder builder() {
		var v = (TableView) ViewDecorator.super.builder().build();
		return ()-> new TableView(v.getSchema(), v.getName()) {
			@Override
			public String sql(QueryParameterBuilder builder) {
				return member(getSchemaOrElse(builder.getSchema()), viewName(v.getName()));
			}
		};
	}

	default String viewName(String name){
		return name;
	}
	
	Collection<T> parseIterable(Map<String, String[]> parameterMap);
	
	@Override
	default void parseFilters(RequestQueryBuilder query, Map<String, String[]> parameters) {
		var it = iterator(parseIterable(parameters));
		var fr = filter(); //optional
		if(nonNull(fr)) {
			query.filters(b-> fr.sql(b));
		}
		ViewDecorator.super.parseFilters(query, parameters);
		query.repeat(it);
	}

	 default DBFilter filter(){
		 return null;
	 }
	
	default T currentModel() {
		return null;
	}
}
