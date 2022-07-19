package org.usf.jquery.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usf.jquery.core.AggregatFunction.COUNT;
import static org.usf.jquery.core.Asserts.assertCallNumber;
import static org.usf.jquery.core.Asserts.assertCallParameter;
import static org.usf.jquery.core.Asserts.assertRequireOneParameter;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

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
