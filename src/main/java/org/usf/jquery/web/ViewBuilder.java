package org.usf.jquery.web;

import static org.usf.jquery.core.Validation.requireNoArgs;
import static org.usf.jquery.web.JQuery.currentEnvironment;

import org.usf.jquery.core.DBView;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ViewBuilder extends Builder<DatabaseDecorator, DBView> {

	DBView build(DatabaseDecorator db, Environment env);
	
	default DBView build(DatabaseDecorator parent) {
		return build(parent, currentEnvironment());
	}

	@Override
	default DBView build(DatabaseDecorator parent, Environment env, String... args) {
		requireNoArgs(args, ()-> "no args expected");
		return this.build(parent, env);
	}	
}
