package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface DBFilter extends DBColumn, Chainable<DBFilter> {
	
	void build(QueryBuilder query);
	
	@Override
	default JDBCType getType() {
		return JDBCType.BOOLEAN;
	}
}