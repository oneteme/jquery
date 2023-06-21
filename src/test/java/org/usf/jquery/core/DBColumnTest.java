package org.usf.jquery.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.DBColumn.constant;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import org.junit.jupiter.api.Test;

class DBColumnTest {

	@Test
	void testIsAggregation() {
		assertFalse(column("col1").isAggregation());
		assertFalse(constant("col1").isAggregation());
	}

	@Test
	void testIsConstant() {
		assertFalse(column("cm1").isConstant());
		assertTrue(constant("cm1").isConstant());
	}
	
	@Test
	void testSql() {
		assertEquals("cm1", column("cm1").sql(addWithValue()));
		assertEquals("'cm1'", constant("cm1").sql(addWithValue()));
		assertEquals("1234", constant(1234).sql(addWithValue()));
	}
	
}
