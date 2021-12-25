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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NullFilterTest {

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToSql(DBColumn column, DBTable table, boolean invert, String sql) {
		assertEquals(sql, new NullFilter(column, invert).sql(table));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testArgs(DBColumn column, DBTable table, boolean invert) {
		assertArrayEquals(new Object[] {}, new NullFilter(column, invert).args().toArray());
	}
	
	@Test
	void testNullFilter(){
		assertThrows(NullPointerException.class, ()-> new NullFilter(null, false));
		assertThrows(NullPointerException.class, ()-> new NullFilter(null, true));
	}
	
	private static Stream<Arguments> caseProvider() {
		
	    return Stream.of(
	    		Arguments.of(c1, tab1, false, c1_name+" IS NULL"),
	    		Arguments.of(c2, tab1, false, c2_name+" IS NULL"),
	    		Arguments.of(c3, tab1, true,  c3_name+" IS NOT NULL"),
	    		Arguments.of(c4, tab1, true,  c4_name+" IS NOT NULL")
	    	);
	}


}
