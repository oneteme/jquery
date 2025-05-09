package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
public final class DistinctOperator implements Operator {

	@Override
	public void build(QueryBuilder query, Object... args) {
		requireAtLeastNArgs(1, args, this::id);
		query.append(id()).appendSpace();
		if(args.length == 1) {
			query.appendParenthesis(()-> query.appendParameter(args[0]));
		}
		else {
			query.appendParameters(",", args); //parenthesis PG !?
		}
	}

	@Override
	public String id() {
		return "DISTINCT";
	}
}
