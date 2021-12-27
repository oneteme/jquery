package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericTable.c1_name;
import static fr.enedis.teme.jquery.GenericTable.c2_name;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OperatorExpressionTest implements ExpressionProvider {

	private final ParameterHolder STAT = addWithValue();
	private final ParameterHolder DYNC = parametrized();

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testSql(Operator op, Object arg, String[] sql) {
		assertEquals(c1_name+sql[0], new OperatorExpression<>(op, arg).sql(c1_name, DYNC));
		assertEquals(c2_name+sql[1], new OperatorExpression<>(op, arg).sql(c2_name, STAT));
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testString(Operator op, Object arg, String[] sql) {
		assertEquals(sql[1], new OperatorExpression<>(op, arg).toString());
	}
	
}
