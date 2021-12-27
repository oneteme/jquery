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
	void testSql(String mappedName, Object expression, DBTable table, String sql) {
		assertEquals(sql, staticColumn(mappedName, expression).sql(table, null));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testTag(String mappedName, Object expression, DBTable table, String sql) {
		assertEquals(mappedName, staticColumn(mappedName, expression).tag(table));
	}
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsConstant(String mappedName, Object expression) {
		assertTrue(staticColumn(mappedName, expression).isConstant());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsExpression(String mappedName, Object expression) {
		assertFalse(staticColumn(mappedName, expression).isExpression());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetMappedName(String mappedName, Object expression) {// 100% coverage
		assertEquals(mappedName, staticColumn(mappedName, expression).getTagName());
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToString(String mappedName, Object expression, DBTable table, String sql) {
		assertEquals(sql, staticColumn(mappedName, expression).toString());
	}

	@ParameterizedTest
	@EmptySource
	@ValueSource(strings = {" ", "*", "$column", "column>3"})
	void testStaticColumn(String tagName) {
		assertThrows(IllegalArgumentException.class, ()-> staticColumn(tagName, null));
	}
	
	@Test
	void testStaticColumn() {
		assertThrows(IllegalArgumentException.class, ()-> staticColumn(null, null));
	}
	
	private static Stream<Arguments> caseProvider() {
	    return Stream.of(
    		//same results with null table
    		Arguments.of("field1", null, null, "null"),
    		Arguments.of("field2", "*", null, "*"),
    		Arguments.of("field3", 123, null, "123"),
    		Arguments.of("field4", "123", null, "'123'"),
    		Arguments.of("field5", LocalDate.of(2022, 1, 1), null, "'2022-01-01'")
	    );
	}

}
