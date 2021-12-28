package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericTable.c1_name;
import static fr.enedis.teme.jquery.GenericTable.c2_name;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OperatorExpressionTest implements DataProvider {

	private final ParameterHolder STAT = addWithValue();
	private final ParameterHolder DYNC = parametrized();

	@ParameterizedTest
	@MethodSource("expressionCaseProvider")
	void testSql(OperatorExpression<?> exp, String[] sql) {
		assertEquals(c1_name+sql[0], exp.sql(c1_name, DYNC));
		assertEquals(c2_name+sql[1], exp.sql(c2_name, STAT));
	}
	
	@ParameterizedTest
	@MethodSource("expressionCaseProvider")
	void testString(OperatorExpression<?> exp, String[] sql) {
		assertEquals(sql[1], exp.toString());
	}	
}
