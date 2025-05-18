package org.usf.jquery.web;

import static org.usf.jquery.web.JQuery.currentContext;

import org.usf.jquery.core.DBView;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ViewBuilder {

	DBView build(DatabaseDecorator db);
	
	default DBView build() {
		return build(currentContext().getEnvironment().getDatabase());
	}
}
