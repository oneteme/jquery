package org.usf.jquery;

import static org.usf.jquery.LogicalOperator.AND;
import static org.usf.jquery.LogicalOperator.OR;

public interface DBFilter extends DBObject {

	DBFilter append(LogicalOperator op, DBFilter filter);

	default DBFilter and(DBFilter filter) {
		return append(AND, filter);
	}

	default DBFilter or(DBFilter filter) {
		return append(OR, filter);
	}
}
