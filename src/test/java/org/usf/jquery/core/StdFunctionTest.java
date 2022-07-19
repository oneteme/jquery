package org.usf.jquery.core;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StdFunctionTest {
	
	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testIsAggregation(StdFunction fn) {
		assertFalse(fn.isAggregate());
	}
	
	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testSql_fail(StdFunction fn) {
		assertThrows(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), null));
		assertThrows(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), 123, 456));
		assertThrows(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), "abc", "def"));
	}

	@ParameterizedTest
	@EnumSource(value=StdFunction.class, names={"ABS", "SQRT", "TRUNC", "CEIL", "FLOOR"})
	void testSql_number(StdFunction fn) {

		var exp = fn.name() + "(%s)";
		assertEquals(format(exp, "col1"), fn.sql(addWithValue(), DBColumn.ofReference("col1")));
		assertEquals(format(exp, 333), fn.sql(addWithValue(), 333));
		assertThrows(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), "abc"));
	}
	
	@ParameterizedTest
	@EnumSource(value=StdFunction.class, names={"LENGTH", "TRIM", "UPPER", "LOWER"})
	void testSql_string(StdFunction fn) {

		var exp = fn.name() + "(%s)";
		assertEquals(format(exp, "col1"), fn.sql(addWithValue(), DBColumn.ofReference("col1")));
		assertEquals(format(exp, "'abc'"), fn.sql(addWithValue(), "abc"));
		assertThrows(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), 123));
	}
	
}
