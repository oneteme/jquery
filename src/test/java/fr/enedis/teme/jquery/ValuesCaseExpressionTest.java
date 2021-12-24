package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.CaseExpressionBuilder.caseWhen;
import static fr.enedis.teme.jquery.ValuesCaseExpression.values;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValuesCaseExpressionTest {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToSql(CaseExpressionBuilder<?> eb, String columnName, String sql) {
		assertEquals(sql, values(eb).toSql(columnName));
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetFunctionName(CaseExpressionBuilder<?> eb) {
		assertEquals("case", values(eb).getFunctionName());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testMappedName(CaseExpressionBuilder<?> eb, String columnName) {
		var ce = values(eb);
		assertEquals("case_" + columnName, ce.mappedName(columnName));
		assertThrows(NullPointerException.class, ()-> ce.mappedName(null));
		assertThrows(IllegalArgumentException.class, ()-> ce.mappedName(""));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsAggregation(CaseExpressionBuilder<?> eb) {
		assertFalse(values(eb).isAggregation());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetValues(CaseExpressionBuilder<?> eb) {//100% coverage
		assertEquals(eb, values(eb).getCb());
	}

	@Test
	void testIntervals() {
		assertThrows(NullPointerException.class, ()-> values(null));
	}
	
	private static Stream<Arguments> caseProvider() {
	    return Stream.of(
    		Arguments.of(caseWhen("someValue", "1","2","3"), "column", 
    				"CASE WHEN column IN('1', '2', '3') THEN 'someValue' END"),
    		Arguments.of(caseWhen("value1", 1).when("value2", 2).when("value3", 3), "trim_column", 
    				"CASE trim_column WHEN 1 THEN 'value1' WHEN 2 THEN 'value2' WHEN 3 THEN 'value3' END"),
    		Arguments.of(caseWhen("FRA", "RENAULT", "PEUGEOT").when("GER", "AUDI", "OPEL", "BMW").when("USA", "FORD").orElse("others"), 
    				"trim_column", "CASE WHEN trim_column IN('RENAULT', 'PEUGEOT') THEN 'FRA' WHEN trim_column IN('AUDI', 'OPEL', 'BMW') THEN 'GER' WHEN trim_column IN('FORD') THEN 'USA' ELSE 'others' END")
	    );
	}



}
