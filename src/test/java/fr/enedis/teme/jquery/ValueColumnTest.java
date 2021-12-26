package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ValueColumn.staticColumn;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ValueColumnTest {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testSql(String mappedName, Object expression, DBTable table, String sql) {
		assertEquals(sql, staticColumn(mappedName, expression).sql(table));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testTag(String mappedName, Object expression, DBTable table, String sql) {
		assertEquals(mappedName, staticColumn(mappedName, expression).tag(table));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsExpression(String mappedName, Object expression) {
		assertFalse(staticColumn(mappedName, expression).isExpression());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsConstant(String mappedName, Object expression) {
		assertTrue(staticColumn(mappedName, expression).isConstant());
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testGetMappedName(String mappedName, Object expression) {// 100% coverage
		assertEquals(mappedName, staticColumn(mappedName, expression).getMappedName());
	}
	

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToString(String mappedName, Object expression, DBTable table, String sql) {
		assertEquals(sql, staticColumn(mappedName, expression).toString());
	}

	
	
	@Test
	void testStaticColumn() {
		assertThrows(NullPointerException.class, ()-> staticColumn(null, null));
		assertThrows(IllegalArgumentException.class, ()-> staticColumn("", null));
		assertThrows(IllegalArgumentException.class, ()-> staticColumn(" ", null));
	}
	
	private static Stream<Arguments> caseProvider() {
		var table = new GenericTable("someTable", emptyMap(), null);
	    return Stream.of(
    		Arguments.of("field1", null, table, "null"),
    		Arguments.of("field2", "*", table, "*"),
    		Arguments.of("field3", 123, table, "123"),
    		Arguments.of("field4", "123", table, "'123'"),
    		//same results with null table
    		Arguments.of("field1", null, null, "null"),
    		Arguments.of("field2", "*", null, "*"),
    		Arguments.of("field3", 123, null, "123"),
    		Arguments.of("field4", "123", null, "'123'")
	    );
	}

}
