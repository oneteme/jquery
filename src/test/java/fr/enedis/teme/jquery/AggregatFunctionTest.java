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
		assertEquals(fn.name().toUpperCase() + "(column)", fn.sql("column"));
		assertEquals(fn.name().toUpperCase() + "(*)", fn.sql("*"));
		assertThrows(NullPointerException.class, ()-> fn.sql(null));
		assertThrows(IllegalArgumentException.class, ()-> fn.sql(""));
	}
	
	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testGetFunctionName(AggregatFunction fn) {
		assertEquals(fn.name(), fn.getFunctionName());
	}

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testMappedName(AggregatFunction fn) {
		assertEquals(fn.name().toLowerCase() + "_column", fn.tag("column"));
		assertThrows(NullPointerException.class, ()-> fn.tag(null));
		assertThrows(IllegalArgumentException.class, ()-> fn.tag(""));
	}
	
	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testIsAggregation(AggregatFunction fn) {
		assertTrue(fn.isAggregate(), "iAggregation=true");
	}
}
