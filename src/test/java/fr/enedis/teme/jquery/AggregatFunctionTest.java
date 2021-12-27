package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AggregatFunctionTest {

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testToSql(AggregatFunction fn) {
		assertEquals(fn.name().toUpperCase() + "(column)", fn.sql("column", null));
		assertEquals(fn.name().toUpperCase() + "(*)", fn.sql("*", null));
		assertThrows(NullPointerException.class, ()-> fn.sql(null, null));
		assertThrows(IllegalArgumentException.class, ()-> fn.sql("", null));
	}
	
	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testGetFunctionName(AggregatFunction fn) {
		assertEquals(fn.name(), fn.getFunctionName());
	}

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testIsAggregation(AggregatFunction fn) {
		assertTrue(fn.isAggregate(), "iAggregation=true");
	}
}
