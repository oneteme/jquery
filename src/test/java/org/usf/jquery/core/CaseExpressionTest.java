package org.usf.jquery.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.WhenExpression.orElse;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CaseExpressionTest {

	@ParameterizedTest(name = "{0}")
	@MethodSource("whenCaseProvider")	
	void testSql_when(String expected, WhenExpression wc) {
		assertEquals(expected, wc.sql(addWithValue(), null));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("elseCaseProvider")	
	void testSql_else(String expected, WhenExpression wc) {
		assertEquals(expected, wc.sql(addWithValue(), null));
	}
	
	static Stream<Arguments> whenCaseProvider(){
		return Stream.of(
			of("WHEN cm1='ab' THEN 'cd'", new WhenExpression(column("cm1").equal("ab"), "cd")),
			of("WHEN cm1>=1234 THEN 5678", new WhenExpression(column("cm1").greaterOrEqual(1234), 5678)),
			of("WHEN cm1 LIKE '%ab%' THEN cm2", new WhenExpression(column("cm1").like("%ab%"), column("cm2"))),
			of("WHEN cm1 IS NOT NULL THEN '0'", new WhenExpression(column("cm1").isNotNull(), "0")));
	}
	
	static Stream<Arguments> elseCaseProvider(){
		return Stream.of(
			of("ELSE null", orElse(null)),
			of("ELSE 'ab'", orElse("ab")),
			of("ELSE 1234", orElse(1234)),
			of("ELSE col1", orElse(column("col1"))));
	}


}
