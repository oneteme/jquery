package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Mappers.keyValueMapper;
import static org.usf.jquery.core.Mappers.toListMapper;
import static org.usf.jquery.core.QueryExecutor.defaultExecutor;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;

import java.util.List;
import java.util.function.Consumer;

import org.usf.jquery.core.View;
import org.usf.jquery.core.DynamicModel;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.QueryExecutor;
import org.usf.jquery.core.ResultSetMapper;
import org.usf.jquery.core.RowMapper;
import org.usf.jquery.core.Table;

/**
 * 
 * @author u$f
 * 
 */
public interface DatabaseDecorator {

	String identity(); //URL
	
	String viewName(ViewDecorator vd); //[schema.]table
	
	default View view(ViewDecorator vd) {
		var tn = viewName(vd);
		if(nonNull(tn)){
		var idx = tn.indexOf('.');
		return idx == -1
				? new Table(requireLegalVariable(tn), null) 
				: new Table(
							requireLegalVariable(tn.substring(idx+1, tn.length())), //schema
							requireLegalVariable(tn.substring(0, idx))); //view
		}
		var b = vd.builder();
		if(nonNull(b)) {
			return b.build(this);
		}
		throw noSuchResourceException(VIEW_PARAM, vd.identity(), identity());
	}

	default List<DynamicModel> execute(Consumer<QueryComposer> cons) {
		return execute(compose(cons), keyValueMapper());
	}
	
	default <T> List<T> execute(Consumer<QueryComposer> cons, RowMapper<T> mapper) {
		return execute(compose(cons), toListMapper(mapper));
	}

	default <T> T execute(Consumer<QueryComposer> cons, ResultSetMapper<T> mapper) {
		return execute(compose(cons), mapper);
	}
	
	default List<DynamicModel> execute(QueryComposer query) {
		return execute(query, keyValueMapper());
	}
	
	default <T> List<T> execute(QueryComposer query, RowMapper<T> mapper) {
		return execute(query, toListMapper(mapper));
	}
	
	default <T> T execute(QueryComposer query, ResultSetMapper<T> mapper) {
		return run(query, defaultExecutor(mapper));
	}
		
	default <T> T run(QueryComposer query, QueryExecutor<T> executor) {
		return getEnvironment(identity()).exec(query, executor);
	}
	
	default QueryComposer compose(Consumer<QueryComposer> cons) {
		return getEnvironment(identity()).query(cons);
	}
}