package org.usf.jquery.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LogicalOperatorTest {

	@ParameterizedTest()
	@EnumSource(value = LogicalOperator.class)
	void testToString(LogicalOperator op) {
		assertEquals(" " + op.sql() + " ", op.sql());
	}

}
