package org.usf.jdbc.jquery;

@FunctionalInterface
public interface DBExpression {

	String sql(QueryParameterBuilder arg, Object operand);
}
