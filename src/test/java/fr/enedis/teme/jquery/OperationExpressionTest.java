package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericTable.c1_name;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OperationExpressionTest implements ExpressionProvider {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testSql(Operator op, String sql, Object arg, ParameterHolder ph) {
		assertEquals(c1_name+sql, new OperationExpression<>(op, arg).sql(c1_name, ph));
	}
	
	@ParameterizedTest
	@MethodSource("caseProviderValuesOnly")
	void testString(Operator op, String sql, Object arg, ParameterHolder ph) {
		assertEquals(sql, new OperationExpression<>(op, arg).toString());
	}
	
}
