package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ValueColumn.staticColumn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ValueColumnTest {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testSql(ValueColumn<?> column, String sql) {
		assertEquals(sql, column.sql(null, null));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testTag(ValueColumn<?> column, String tag) {
		assertTrue(column.tagname().matches("field[1-5]"));
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

	@Test
	void testStaticColumn() {
		assertThrows(NullPointerException.class, ()-> staticColumn(null, null));
	}
	
	@ParameterizedTest
	@EmptySource
	@ValueSource(strings = {" ", "*", "$column", "column>3"})
	void testStaticColumn(String tagName) {
		assertThrows(IllegalArgumentException.class, ()-> staticColumn(tagName, null));
	}
	
	private static Stream<Arguments> caseProvider() {
	    return Stream.of(
    		//same results with null table
    		Arguments.of(staticColumn("field1", null), "null"),
    		Arguments.of(staticColumn("field2", "*"), "*"),
    		Arguments.of(staticColumn("field3", 123), "123"),
    		Arguments.of(staticColumn("field4", "123"), "'123'"),
    		Arguments.of(staticColumn("field5", LocalDate.of(2022, 1, 1)), "'2022-01-01'")
	    );
	}

}
