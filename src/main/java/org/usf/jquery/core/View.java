package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface View extends QueryPart {

	void build(SqlBuilder builder);
	
	@Override
	default int prepare(QueryAnalyzer analyzer) {
		return SCALAR; //do not declare self on analyzer
	}
	
	@Override
	default void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, View.class::getSimpleName);
		build(builder);
	}
	
	/**return a new instance of the same view, but not the same reference **/
	default View mirror() {
		return new View() {
			
			@Override
			public void build(SqlBuilder builder) {
				View.this.build(builder);				
			}
			
			@Override
			public int prepare(QueryAnalyzer analyzer) {
				return View.this.prepare(analyzer);
			}
		};
	}
}
