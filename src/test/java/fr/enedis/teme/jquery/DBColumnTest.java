package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.GenericColumn.c2;
import static fr.enedis.teme.jquery.GenericColumn.c3;
import static fr.enedis.teme.jquery.GenericColumn.c4;
import static fr.enedis.teme.jquery.GenericTable.c1_name;
import static fr.enedis.teme.jquery.GenericTable.c2_name;
import static fr.enedis.teme.jquery.GenericTable.c3_name;
import static fr.enedis.teme.jquery.GenericTable.c4_name;
import static fr.enedis.teme.jquery.GenericTable.tab1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DBColumnTest {
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToSql(DBColumn column, DBTable table, String sql) {
		assertEquals(sql, column.sql(table, null));
		assertThrows(NullPointerException.class, ()-> column.sql(null, null));
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsAggregated(DBColumn column) {
		assertFalse(column.isAggregation());
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsExpression(DBColumn column) {
		assertFalse(column.isExpression());
	}
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testIsConstant(DBColumn column) {
		assertFalse(column.isConstant());
	}

	private static Stream<Arguments> caseProvider() {
	    return Stream.of(
	    		Arguments.of(c1, tab1, c1_name),
	    		Arguments.of(c2, tab1, c2_name),
	    		Arguments.of(c3, tab1, c3_name),
	    		Arguments.of(c4, tab1, c4_name)
	    	);
	}
}
