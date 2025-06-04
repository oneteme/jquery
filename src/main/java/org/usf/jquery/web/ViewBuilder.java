package org.usf.jquery.web;

import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.DBView;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ViewBuilder extends Builder<DatabaseDecorator, DBView> {

	DBView build(DatabaseDecorator db);
	
	@Override
	default DBView build(DatabaseDecorator parent, String... args) {
		requireNoArgs(args, ()-> "no args expected");
		return this.build(parent);
	}	
}
