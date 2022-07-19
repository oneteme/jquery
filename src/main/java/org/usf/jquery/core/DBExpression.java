package org.usf.jquery.core;

@FunctionalInterface
public interface DBExpression {

	String sql(QueryParameterBuilder arg, Object operand);
}
