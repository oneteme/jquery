package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.GenericColumn.c2;
import static fr.enedis.teme.jquery.GenericColumn.c3;
import static fr.enedis.teme.jquery.GenericColumn.c4;
import static fr.enedis.teme.jquery.GenericTable.*;
import static fr.enedis.teme.jquery.GenericTable.tab1;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InFilterTest {
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testToSql(DBColumn column, DBTable table, boolean invert, List<Object> values, String sql) {
		assertEquals(sql, new InFilter<>(column, invert, values.toArray()).sql(table));
	}

	@ParameterizedTest
	@MethodSource("caseProvider")
	void testArgs(DBColumn column, DBTable table, boolean invert, List<Object> values) { // 100% coverage
		assertArrayEquals(values.toArray(), new InFilter<>(column, invert, values.toArray()).args().toArray());
	}
	
	@Test
	void testInFilter(){
		Object[] arr = null;
		assertThrows(NullPointerException.class, ()-> new InFilter(null, false, arr));
		assertThrows(NullPointerException.class, ()-> new InFilter(c1, true, arr));
		assertThrows(NullPointerException.class, ()-> new InFilter(null, true));
		assertThrows(IllegalArgumentException.class, ()-> new InFilter(c1, true));
	}

	private static Stream<Arguments> caseProvider() {
		
	    return Stream.of(
	    		Arguments.of(c1, tab1, false, asList(1), c1_name+"=?"), //int
	    		Arguments.of(c2, tab1, false, asList(.2, .1), c2_name+" IN(?,?)"), //double
	    		Arguments.of(c3, tab1, false, asList("A", "B", "C"), c3_name+" IN(?,?,?)"), //char
	    		Arguments.of(c4, tab1, false, asList(LocalDate.of(2020, 1, 1)), c4_name+"=?"), //Date
	    		//invert
	    		Arguments.of(c1, tab1, true, asList(1), c1_name+"<>?"), //int
	    		Arguments.of(c2, tab1, true, asList(.2, .1), c2_name+" NOT IN(?,?)"), //double
	    		Arguments.of(c3, tab1, true, asList("A", "B", "C"), c3_name+" NOT IN(?,?,?)"), //char
	    		Arguments.of(c4, tab1, true, asList(LocalDate.of(2020, 1, 1)), c4_name+"<>?") //Date
	    	);
	}


}
