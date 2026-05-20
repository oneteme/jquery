package org.usf.jquery.web;

import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.View;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ViewBuilder extends Builder<DatabaseDecorator, View> {

	View build(DatabaseDecorator db);
	
	@Override
	default View build(DatabaseDecorator parent, String... args) {
		requireNoArgs(args, ()-> "no args expected");
		return this.build(parent);
	}	
}
