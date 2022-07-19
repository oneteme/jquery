package org.usf.jdbc.jquery;

import static org.usf.jdbc.jquery.LogicalOperator.AND;
import static org.usf.jdbc.jquery.LogicalOperator.OR;

public interface DBFilter extends DBObject {

	DBFilter append(LogicalOperator op, DBFilter filter);

	default DBFilter and(DBFilter filter) {
		return append(AND, filter);
	}

	default DBFilter or(DBFilter filter) {
		return append(OR, filter);
	}
}
