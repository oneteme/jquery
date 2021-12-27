package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StdFunctionTest {

	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testToSql(StdFunction fn) {
		assertEquals(fn.name().toUpperCase() + "(column)", fn.sql("column", null));
		assertThrows(NullPointerException.class, ()-> fn.sql(null, null));
		assertThrows(IllegalArgumentException.class, ()-> fn.sql("", null));
	}

	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testIsAggregation(StdFunction fn) {
		assertFalse(fn.isAggregate());
	}
	
	@ParameterizedTest
	@EnumSource(StdFunction.class)
	void testGetFunctionName(StdFunction fn) {
		assertEquals(fn.name(), fn.getFunctionName());
	}

}
