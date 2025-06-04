package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Mappers.keyValue;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;

import java.util.List;
import java.util.function.Consumer;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.DynamicModel;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.ResultSetMapper;
import org.usf.jquery.core.TableView;

/**
 * 
 * @author u$f
 * 
 */
public interface DatabaseDecorator {

	String identity(); //URL
	
	String viewName(ViewDecorator vd); //[schema.]table
	
	default DBView view(ViewDecorator vd) {
		var tn = viewName(vd);
		if(nonNull(tn)){
		var idx = tn.indexOf('.');
		return idx == -1
				? new TableView(requireLegalVariable(tn), null, identity()) 
				: new TableView(
							requireLegalVariable(tn.substring(idx+1, tn.length())), //schema
							requireLegalVariable(tn.substring(0, idx)), identity()); //view
		}
		var b = vd.builder();
		if(nonNull(b)) {
			return b.build(this);
		}
		throw noSuchResourceException(VIEW_PARAM, vd.identity(), identity());
	}

	default List<DynamicModel> execute(Consumer<QueryComposer> fn) {
		return execute(compose(fn), keyValue());
	}

	default <T> T execute(Consumer<QueryComposer> fn, ResultSetMapper<T> rsm) {
		return execute(compose(fn), rsm);
	}
	
	default List<DynamicModel> execute(QueryComposer query) {
		return execute(query, keyValue());
	}
	
	default <T> T execute(QueryComposer query, ResultSetMapper<T> rsm) {
		return getEnvironment(identity()).exec(query, rsm);
	}
	
	default QueryComposer compose(Consumer<QueryComposer> fn) {
		return getEnvironment(identity()).query(fn);
	}
}