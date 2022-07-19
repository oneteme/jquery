package org.usf.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usf.jquery.DBColumn.ofConstant;
import static org.usf.jquery.DBColumn.ofReference;
import static org.usf.jquery.QueryParameterBuilder.addWithValue;

import org.junit.jupiter.api.Test;

class DBColumnTest {

	@Test
	void testIsAggregation() {
		assertFalse(ofReference("col1").isAggregation());
		assertFalse(ofConstant("col1").isAggregation());
	}

	@Test
	void testIsConstant() {
		assertFalse(ofReference("cm1").isConstant());
		assertTrue(ofConstant("cm1").isConstant());
	}
	
	@Test
	void testSql() {
		assertEquals("cm1", ofReference("cm1").sql(addWithValue()));
		assertEquals("'cm1'", ofConstant("cm1").sql(addWithValue()));
		assertEquals("1234", ofConstant(1234).sql(addWithValue()));
	}
	
	@Test
	void testAs() {
		assertEquals("cm1 AS lb1", ofReference("cm1").as("lb1").tagSql(addWithValue()));
		assertEquals("'cm1' AS lb1", ofConstant("cm1").as("lb1").tagSql(addWithValue()));
		assertEquals("1234 AS lb1", ofConstant(1234).as("lb1").tagSql(addWithValue()));
	}
}
