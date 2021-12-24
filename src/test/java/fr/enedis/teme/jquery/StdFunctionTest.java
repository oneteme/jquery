package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StdFunctionTest {
	
	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testGetFunctionName(StdFunction fn) {
		assertEquals(fn.name(), fn.getFunctionName(), "functionName=enum::name");
	}

	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testIsAggregation(StdFunction fn) {
		assertFalse(fn.isAggregation(), "iAggregation=false");
	}

	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testMappedName(StdFunction fn) {
		assertEquals(fn.name().toLowerCase() + "_column", fn.mappedName("column"));
		assertThrows(NullPointerException.class, ()-> fn.mappedName(null));
		assertThrows(IllegalArgumentException.class, ()-> fn.mappedName(""));
	}
	
	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testToSql(StdFunction fn) {
		assertEquals(fn.name().toUpperCase() + "(column)", fn.toSql("column"));
		assertThrows(NullPointerException.class, ()-> fn.toSql(null));
		assertThrows(IllegalArgumentException.class, ()-> fn.toSql(""));
	}
}
