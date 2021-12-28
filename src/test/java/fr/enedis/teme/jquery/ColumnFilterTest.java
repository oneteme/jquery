package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.GenericTable.tab1;
import static fr.enedis.teme.jquery.Helper.fieldValue;
import static fr.enedis.teme.jquery.OperatorExpression.isNull;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ColumnFilterTest implements DataProvider {

	private final ParameterHolder STAT = addWithValue();
	private final ParameterHolder DYNC = parametrized();

	@ParameterizedTest
	@MethodSource("filterCaseProvider")
	void testSql(DBFilter filter, String[] sql) {
		DBColumn c = (DBColumn) fieldValue("column", filter);
		assertEquals(tab1.getColumnName(c)+sql[0], filter.sql(tab1, DYNC));
		assertEquals(tab1.getColumnName(c)+sql[1], filter.sql(tab1, STAT));
	}
	
	@ParameterizedTest
	@MethodSource("filterCaseProvider")
	void testString(DBFilter filter, String[] sql) {
		assertEquals("${column}"+sql[1], filter.toString());
	}
	
	@Test
	void testColumnFilter(){
		var exp = isNull();
		assertThrows(NullPointerException.class, ()-> new ColumnFilter(null, null));
		assertThrows(NullPointerException.class, ()-> new ColumnFilter(c1, null));
		assertThrows(NullPointerException.class, ()-> new ColumnFilter(null, exp));
	}
	
}
