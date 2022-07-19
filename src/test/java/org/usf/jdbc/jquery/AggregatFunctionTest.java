package org.usf.jdbc.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usf.jdbc.jquery.AggregatFunction.COUNT;
import static org.usf.jdbc.jquery.Asserts.assertCallNumber;
import static org.usf.jdbc.jquery.Asserts.assertCallParameter;
import static org.usf.jdbc.jquery.Asserts.assertRequireOneParameter;
import static org.usf.jdbc.jquery.QueryParameterBuilder.addWithValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class AggregatFunctionTest {

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testIsAggregation(AggregatFunction fn) {
		assertTrue(fn.isAggregate());
	}

	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names={"SUM", "AVG"})
	void testSql_number(AggregatFunction fn) {

		assertRequireOneParameter(fn);
		assertCallNumber((o1,__)-> fn.name()+"("+o1+")", (p, op, __)-> fn.sql(p, op));
	}
	
	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names={"COUNT", "MIN", "MAX"})
	void testSql_value(AggregatFunction fn) {

		assertRequireOneParameter(fn);
		assertCallParameter((o1,__)-> fn.name()+"("+o1+")", (p, op, __)-> fn.sql(p, op));
	}

	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names="COUNT", mode=Mode.EXCLUDE)
	void testOfAll(AggregatFunction fn) {
		assertThrows(IllegalArgumentException.class, fn::ofAll);
	}
	
	@Test
	void testOfAll() {
		assertEquals("COUNT(*)", COUNT.ofAll().sql(addWithValue()));
	}
}
