package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;

public interface DBFilter extends DBObject {

	DBFilter append(LogicalOperator op, DBFilter filter);

	default DBFilter and(DBFilter filter) {
		return append(AND, filter);
	}

	default DBFilter or(DBFilter filter) {
		return append(OR, filter);
	}
}
