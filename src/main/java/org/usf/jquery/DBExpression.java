package org.usf.jquery;

@FunctionalInterface
public interface DBExpression {

	String sql(QueryParameterBuilder arg, Object operand);
}
