package org.usf.jquery.core;

import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;

import java.time.LocalDate;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;

class QueryParameterBuilderTest {

	@Test
	void testAppendParameter() {
		assertNotArray();//not array
		assertNullParam(QueryParameterBuilder::appendParameter);
		assertParamEquals("col1", "col1", DBColumn.ofReference("col1"), QueryParameterBuilder::appendParameter);
		assertParamEquals("'abc'", "?", "abc", QueryParameterBuilder::appendParameter);
		assertParamEquals("123", "?", 123, QueryParameterBuilder::appendParameter);
	}

	@Test
	void testAppendString() {
		assertNotArray();//not array
		assertNullParam(QueryParameterBuilder::appendString);
		assertParamEquals("col1", "col1", DBColumn.ofReference("col1"), QueryParameterBuilder::appendString);
		assertParamEquals("'abc'", "?", "abc", QueryParameterBuilder::appendString);
	}
	
	@Test
	void testAppendNumber() {
		assertNotArray();//not array
		assertNullParam(QueryParameterBuilder::appendNumber);		
		assertParamEquals("col1", "col1", DBColumn.ofReference("col1"), QueryParameterBuilder::appendNumber);
		assertParamEquals("123", "?", 123, QueryParameterBuilder::appendNumber);
		assertParamEquals("1", "?", ONE, QueryParameterBuilder::appendNumber);
	}
	
	@Test
	void testAppendArray() {
		assertNot(IllegalArgumentException.class, null, QueryParameterBuilder::appendArray); //null
		assertNot(IllegalArgumentException.class, 123, QueryParameterBuilder::appendArray); //number
		assertNot(IllegalArgumentException.class, "abc", QueryParameterBuilder::appendArray); //string
		assertNot(IllegalArgumentException.class, new int[] {}, QueryParameterBuilder::appendArray); //empty array
		assertNot(IllegalArgumentException.class, new String[] {}, QueryParameterBuilder::appendArray); //empty array

		assertParamEquals("1,2,3", "?,?,?", new int[] {1,2,3}, QueryParameterBuilder::appendArray);
		assertParamEquals("1,2,3", "?,?,?", new int[] {1,2,3}, QueryParameterBuilder::appendArray);
		assertParamEquals("'a','b','c'", "?,?,?", new String[] {"a","b","c"}, QueryParameterBuilder::appendArray);
		assertParamEquals("'2020-01-01'", "?", new Object[] {LocalDate.of(2020,1,1)}, QueryParameterBuilder::appendArray);
	}
	
	@Test
	void testArgs_parametrized() {
		var p = parametrized();
		p.appendArray(new int[] {1,2,3});
		p.appendNumber(null);
		p.appendString("abc");
		p.appendParameter(DBColumn.ofReference("col1"));
		assertArrayEquals(new Object[] {1,2,3, null, "abc"}, p.args());
	}
	
	@Test
	void testArgs_addWithValue() {
		var p = addWithValue();
		p.appendArray(new int[] {1,2,3});
		p.appendNumber(null);
		p.appendString("abc");
		p.appendParameter(DBColumn.ofReference("col1"));
		assertArrayEquals(new Object[0], p.args());
	}

	private static void assertNullParam(BiFunction<QueryParameterBuilder, Object, String> fn) {
		assertParamEquals("null", "?", null, fn);
	}
	
	private void assertNotArray() {
		assertNot(IllegalArgumentException.class, new int[] {}, QueryParameterBuilder::appendParameter);
		assertNot(IllegalArgumentException.class, new String[] {"123"}, QueryParameterBuilder::appendParameter);
		assertNot(IllegalArgumentException.class, new Object[] {LocalDate.now(), 3.14}, QueryParameterBuilder::appendParameter);
	}

	private static void assertParamEquals(String addWithValue, String parametrized, Object o, BiFunction<QueryParameterBuilder, Object, String> fn) { assertEquals(addWithValue, fn.apply(addWithValue(), o));
		assertEquals(parametrized, fn.apply(parametrized(), o));
	}
	
	private void assertNot(Class<? extends Exception> clazz, Object o, BiFunction<QueryParameterBuilder, Object, String> fn) {
		assertThrows(clazz, ()-> fn.apply(addWithValue(), o));
		assertThrows(clazz, ()-> fn.apply(parametrized(), o));
	}
}
