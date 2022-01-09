package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.Helper.fieldValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class AggregatFunctionTest {

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testToSql(AggregatFunction fn) {
		assertEquals(fn.name().toUpperCase() + "(column)", fn.sql("column", null));
//		assertThrows(NullPointerException.class, ()-> fn.sql(null, null));
//		assertThrows(IllegalArgumentException.class, ()-> fn.sql("", null));
	}

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testIsAggregation(AggregatFunction fn) {
		assertTrue(fn.isAggregate());
	}
	
	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testGetFunctionName(AggregatFunction fn) {
		assertEquals(fn.name(), fn.name());
	}

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testOf(AggregatFunction fn) {
		assertThrows(NullPointerException.class, ()-> fn.of(null));
		var fc = fn.of(c1);
		assertEquals(fn, fieldValue("function", fc));
		assertEquals(c1, fieldValue("column", fc));
	}
	
	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names = "COUNT", mode = Mode.EXCLUDE)
	void testOfAll(AggregatFunction fn) {
		assertThrows(IllegalArgumentException.class, ()-> fn.ofAll());
	}

	@ParameterizedTest
	@EnumSource(value=AggregatFunction.class, names = "COUNT")
	void testCountOfAll(AggregatFunction fn) {
		var fc = fn.ofAll();
		assertEquals(fn, fieldValue("function", fc));
		var c = fieldValue("column", fc);
		assertInstanceOf(ValueColumn.class, c);
		assertEquals("*", fieldValue("value", c));
		assertEquals("all", fieldValue("tagName", c));
	}
}
