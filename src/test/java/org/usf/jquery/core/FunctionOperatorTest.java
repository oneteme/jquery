package org.usf.jquery.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.usf.jquery.core.QueryVariables.addWithValue;

import org.junit.jupiter.api.Test;

class FunctionOperatorTest {

	@Test
	void testSql() {
		FunctionOperator fn = ()-> "dummy";
		assertEquals("dummy()", fn.sql(addWithValue(), new Object[] {}));
	}
	
	@Test
	void testSql2() {
		FunctionOperator fn = ()-> "dummy";
		assertEquals("dummy('toto', 123)", fn.sql(addWithValue(), new Object[] {"toto", 123}));
	}
	
	@Test
	void testSql3() {
		FunctionOperator fn = ()-> "dummy";
		DBColumn col = b-> "col1";
		assertEquals("dummy(col1, 123)", fn.sql(addWithValue(), new Object[] {col, 123}));
	}
	
	@Test
	void testSql4() {
		FunctionOperator fn = ()-> "dummy";
		DBColumn col1 = b-> "col1";
		DBColumn col2 = b-> "col2";
		DBColumn col3 = b-> "col3";
		assertEquals("dummy(col1, col2, col3)", fn.sql(addWithValue(), new Object[] {col1, col2, col3}));
	}

}
