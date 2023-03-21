package org.usf.jquery.core;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StdFunctionTest {
	
	@ParameterizedTest
	@EnumSource(NumericFunction.class)
	void testIsAggregation(NumericFunction fn) {
		assertFalse(fn.isAggregate());
	}
	
	@ParameterizedTest
	@EnumSource(NumericFunction.class)
	void testSql_fail(NumericFunction fn) {
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), null));
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), new Object[] {}));
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), new Object[] {123, 456}));
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), new Object[] {"abc", "def"}));
	}

	@ParameterizedTest
	@EnumSource(value=NumericFunction.class, names={"ABS", "SQRT", "TRUNC", "CEIL", "FLOOR"})
	void testSql_number(NumericFunction fn) {

		var exp = fn.identity() + "(%s)";
		assertEquals(format(exp, "col1"), fn.sql(addWithValue(), new Object[] {column("col1")}));
		assertEquals(format(exp, 333), fn.sql(addWithValue(), new Object[] {333}));
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), new Object[] {"abc"}));
	}
	
	@ParameterizedTest
	@EnumSource(value=NumericFunction.class, names={"LENGTH", "TRIM", "UPPER", "LOWER"})
	void testSql_string(NumericFunction fn) {

		var exp = fn.identity() + "(%s)";
		assertEquals(format(exp, "col1"), fn.sql(addWithValue(), new Object[] {column("col1")}));
		assertEquals(format(exp, "'abc'"), fn.sql(addWithValue(), new Object[] {"abc"}));
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(addWithValue(), new Object[] {123}));
	}
	
}
