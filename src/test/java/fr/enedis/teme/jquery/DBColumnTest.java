package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBColumn.ofConstant;
import static fr.enedis.teme.jquery.DBColumn.ofReference;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
