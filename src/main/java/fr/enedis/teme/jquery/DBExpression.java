package fr.enedis.teme.jquery;

public interface DBExpression {

	String sql(QueryParameterBuilder arg, Object operand);
}
