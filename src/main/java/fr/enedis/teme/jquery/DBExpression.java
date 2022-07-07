package fr.enedis.teme.jquery;

@FunctionalInterface
public interface DBExpression {

	String sql(QueryParameterBuilder arg, Object operand);
}
