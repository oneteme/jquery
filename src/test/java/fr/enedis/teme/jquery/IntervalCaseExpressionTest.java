package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.IntervalCaseExpression.intervals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IntervalCaseExpressionTest {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToSql(Object serie, String columnName, String sql) {
		assertEquals(sql, create(serie).toSql(columnName));
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetFunctionName(Object serie) {
		assertEquals("case", create(serie).getFunctionName());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testMappedName(Object serie, String columnName) {
		var ce = create(serie);
		assertEquals("case_" + columnName, ce.mappedName(columnName));
		assertThrows(NullPointerException.class, ()-> ce.mappedName(null));
		assertThrows(IllegalArgumentException.class, ()-> ce.mappedName(""));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsAggregation(Object serie) {
		assertFalse(create(serie).isAggregation());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetValues(Object serie, String columnName, String sql, Number[] values) {//100% coverage
		assertArrayEquals(values, create(serie).getValues());
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
    		Arguments.of(new int[]{1,2}, "column", 
    				"CASE WHEN column<1 THEN 'lt_1' WHEN column>=1 AND column<2 THEN 'bt_1_2' WHEN column>=2 THEN 'gt_2' END", 
    				new Number[] {1,2}), //int 
    		Arguments.of(new double[]{.12}, "trim_column", "CASE WHEN trim_column<0.12 THEN 'lt_0.12' WHEN trim_column>=0.12 THEN 'gt_0.12' END", 
    				new Number[] {.12}), //double
    		Arguments.of(new double[]{.77, .01, .12}, "index", "CASE WHEN index<0.01 THEN 'lt_0.01' WHEN index>=0.01 AND index<0.12 THEN 'bt_0.01_0.12' WHEN index>=0.12 AND index<0.77 THEN 'bt_0.12_0.77' WHEN index>=0.77 THEN 'gt_0.77' END", 
    				new Number[] {.01, .12, .77})//unordered array 
	    );
	}

	private static IntervalCaseExpression create(Object o) {
		if(int[].class.isInstance(o)) {
			return intervals((int[])o);
		}
		if(double[].class.isInstance(o)) {
			return intervals((double[])o);
		}
		throw new UnsupportedOperationException();
	}
}
