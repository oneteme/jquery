package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ValueColumn.staticColumn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValueColumnTest {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testSql(ValueColumn<?> column, String sql) {
		assertEquals(sql, column.sql(null, null));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsConstant(ValueColumn<?> column) {
		assertTrue(column.isConstant());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsExpression(ValueColumn<?> column) {
		assertFalse(column.isExpression());
	}
	

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsAggregatio(ValueColumn<?> column) {
		assertFalse(column.isAggregation());
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToString(ValueColumn<?> column, String value) {
		assertEquals(value, column.toString());
	}
	
	private static Stream<Arguments> caseProvider() {
	    return Stream.of(
    		//same results with null table
    		Arguments.of(staticColumn(null), "null"),
    		Arguments.of(staticColumn("*"), "'*'"),
    		Arguments.of(staticColumn(123), "123"),
    		Arguments.of(staticColumn("123"), "'123'"),
    		Arguments.of(staticColumn(LocalDate.of(2022, 1, 1)), "'2022-01-01'")
	    );
	}

}
