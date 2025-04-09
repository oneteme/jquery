package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface CastFunction extends FunctionOperator {

	String type();
	
	@Override
	default String id() {
		return "CAST";
	}
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireAtLeastNArgs(1, args, CastFunction.class::getSimpleName);
		query.append(id()).parenthesis(()-> {
			query.appendLiteral(args[0]).appendAs().append(type());
			if(args.length > 1) { //varchar | decimal
				query.parenthesis(()-> query.appendLiteralArray(SCOMA, args, 1));
			}
		});
	}
}
