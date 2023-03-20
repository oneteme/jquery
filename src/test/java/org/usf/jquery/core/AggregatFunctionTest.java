package org.usf.jquery.core;

import static java.math.BigDecimal.valueOf;
import static java.sql.Date.valueOf;
import static java.sql.Timestamp.from;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usf.jquery.core.AggregatFunction.COUNT;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class AggregatFunctionTest {

	private final QueryParameterBuilder builder = addWithValue();

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testIsAggregation(AggregatFunction fn) {
		assertTrue(fn.isAggregate());
	}

	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names={"SUM", "AVG"})
	void testSql_number(AggregatFunction fn) {
		assertEquals(fn.name() + "(dummy)", fn.sql(builder, new Object[] {column("dummy")}));
		assertEquals(fn.name() + "(12345)", fn.sql(builder, new Object[] {12345}));
		assertEquals(fn.name() + "(12.45)", fn.sql(builder, new Object[] {12.45}));
		assertEquals(fn.name() + "(123.5)", fn.sql(builder, new BigDecimal[] {valueOf(123.5)}));
		assertEquals(fn.name() + "(12345, 'dummy')", fn.sql(builder, new Object[] {new AtomicInteger(12345), "dummy"}));
	}
	
	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names={"SUM", "AVG"})
	void testSql_number_illegal(AggregatFunction fn) {
		test_any_illegal(fn);
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(builder, new Object[] {""})); //string arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(builder, new Object[] {'a'})); //char arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(builder, new Object[] {from(now())})); //char arguments
	}
	
	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names={"COUNT", "MIN", "MAX"})
	void testSql_any(AggregatFunction fn) {
		testSql_number(fn);
		assertEquals(fn.name() + "('a')", fn.sql(builder, new Object[] {'a'}));
		assertEquals(fn.name() + "('dummy')", fn.sql(builder, new String[] {"dummy"}));
		assertEquals(fn.name() + "('2020-01-01', 123)", fn.sql(builder, new Object[] {valueOf(LocalDate.of(2020, 1, 1)), 123}));
	}

	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names={"SUM", "AVG"})
	void test_any_illegal(AggregatFunction fn) {
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(builder, null)); //null arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> fn.sql(builder, new Object[] {})); //empty arguments
	}
	
	@Test
	void testOfAll() {
		assertEquals("COUNT(*)", COUNT.ofAll().sql(builder));
	}

	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names="COUNT", mode=Mode.EXCLUDE)
	void testOfAll(AggregatFunction fn) {
		assertThrows(IllegalArgumentException.class, fn::ofAll);
	}
}
