package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.JQuery.context;
import static org.usf.jquery.web.JQuery.getEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.undeclaredResouceException;

import java.util.function.UnaryOperator;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.QueryComposer;
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
							requireLegalVariable(tn.substring(idx+1, tn.length())),
							requireLegalVariable(tn.substring(0, idx)), identity());
		}
		var b = vd.builder();
		if(nonNull(b)) {
			return b.build(this);
		}
		throw undeclaredResouceException(vd.identity(), identity());
	}

	default QueryComposer query(UnaryOperator<QueryComposer> fn) {
		return context(getEnvironment(identity()), ctx-> ctx.query(fn));
	}
}