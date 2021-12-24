package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.IntervalCaseExpression.intervals;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IntervalCaseExpressionTest {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToSql(List<Number> serie, String columnName, String sql) {
		assertEquals(sql, intervals(serie.stream()).toSql(columnName));
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetFunctionName(List<Number> serie) {
		assertEquals("case", intervals(serie.stream()).getFunctionName());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testMappedName(List<Number> serie, String columnName) {
		var ce = intervals(serie.stream());
		assertEquals("case_" + columnName, ce.mappedName(columnName));
		assertThrows(NullPointerException.class, ()-> ce.mappedName(null));
		assertThrows(IllegalArgumentException.class, ()-> ce.mappedName(""));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsAggregation(List<Number> serie) {
		assertFalse(intervals(serie.stream()).isAggregation());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetValues(List<Number> serie) {//100% coverage
		assertArrayEquals(serie.toArray(), intervals(serie.stream()).getValues());
	}
	
	@Test
	void testIntervals() {
		assertThrows(NullPointerException.class, ()-> intervals((int[])null));
		assertThrows(IllegalArgumentException.class, ()-> intervals(new int[]{}));
		assertThrows(NullPointerException.class, ()-> intervals((double[])null));
		assertThrows(IllegalArgumentException.class, ()-> intervals(new double[]{}));
	}

	private static Stream<Arguments> caseProvider() {
	    return Stream.of(
    		Arguments.of(asList(1,2), "column", "CASE WHEN column<1 THEN 'lt_1' WHEN column>=1 AND column<2 THEN 'bt_1_2' WHEN column>=2 THEN 'gt_2' END"),
    		Arguments.of(asList(.12), "trim_column", "CASE WHEN trim_column<0.12 THEN 'lt_0.12' WHEN trim_column>=0.12 THEN 'gt_0.12' END")
	    );
	}

}
