package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public class ValueColumn implements Column {
	
	private final Object value;
	private final JDBCType type;
	
	public ValueColumn(Object value, JDBCType type) {
		if(value == null || value instanceof QueryPart) {
			throw new IllegalArgumentException("ValueColumn only accepts non null scalar value");
		}
		this.value = value;
		this.type = type;
	}

	@Override
	public int prepare(QueryAnalyzer manifest) {
		return SCALAR;
	}
	
	@Override
	public void build(SqlBuilder builder) {
		builder.appendParameter(value);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}

	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
