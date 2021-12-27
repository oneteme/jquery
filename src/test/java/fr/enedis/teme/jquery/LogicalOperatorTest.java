package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class LogicalOperatorTest {

	@ParameterizedTest()
	@EnumSource(value = LogicalOperator.class)
	void testToString(LogicalOperator op) {
		assertEquals(op.toString(), " " + op.name() + " ");
	}

}
