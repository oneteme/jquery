package org.usf.jdbc.jquery;

import static org.usf.jdbc.jquery.Asserts.assertCallNumber;
import static org.usf.jdbc.jquery.Asserts.assertRequireTwoParameter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ArithmeticOperatorTest  {

	@ParameterizedTest
	@EnumSource(ArithmeticOperator.class)
	void testSql(ArithmeticOperator op) {
		assertRequireTwoParameter(op);
		assertCallNumber((o1, o2)-> o1+op.symbol+o2, op);
	}
	
}
