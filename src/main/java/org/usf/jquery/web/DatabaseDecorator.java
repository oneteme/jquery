package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireLegalVariable;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.TableView;

/**
 * 
 * @author u$f
 * 
 */
public interface DatabaseDecorator {

	String identity(); //URL
	
	String viewName(ViewDecorator vd); //[schema.]table

	default DBView tableView(ViewDecorator vd) {
		var tn = viewName(vd);
		if(nonNull(tn)){
			var idx = tn.indexOf('.');
			return idx == -1 
					? new TableView(requireLegalVariable(tn)) 
					: new TableView(requireLegalVariable(tn.substring(0, idx)),
							requireLegalVariable(tn.substring(idx, tn.length())));
		}
		var b = vd.builder();
		if(nonNull(b)){
			return b.build(vd.identity()); //safe ?
		}
		throw new IllegalStateException("require viewName or builder on " + vd);
	}
}
