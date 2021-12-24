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
		assertEquals(fn.name().toUpperCase() + "(column)", fn.toSql("column"));
		assertEquals(fn.name().toUpperCase() + "(*)", fn.toSql("*"));
		assertThrows(NullPointerException.class, ()-> fn.toSql(null));
		assertThrows(IllegalArgumentException.class, ()-> fn.toSql(""));
	}
	
	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testGetFunctionName(AggregatFunction fn) {
		assertEquals(fn.name(), fn.getFunctionName(), "functionName=enum::name");
	}

	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testMappedName(AggregatFunction fn) {
		assertEquals(fn.name().toLowerCase() + "_column", fn.mappedName("column"));
		assertThrows(NullPointerException.class, ()-> fn.mappedName(null));
		assertThrows(IllegalArgumentException.class, ()-> fn.mappedName(""));
	}
	
	@ParameterizedTest
	@EnumSource(AggregatFunction.class)
	void testIsAggregation(AggregatFunction fn) {
		assertTrue(fn.isAggregation(), "iAggregation=true");
	}
}
