package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;

public interface DBFilter extends DBObject<DBTable> {

	DBFilter append(LogicalOperator op, DBFilter filter);

	default DBFilter and(DBFilter filter) {
		return append(AND, filter);
	}

	default DBFilter or(DBFilter filter) {
		return append(OR, filter);
	}
}
