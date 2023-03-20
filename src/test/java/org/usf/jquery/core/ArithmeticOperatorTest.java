package org.usf.jquery.core;

import static java.sql.Timestamp.from;
import static java.time.Instant.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ArithmeticOperatorTest  {
	
	private final QueryParameterBuilder builder = addWithValue();

	@ParameterizedTest
	@EnumSource(ArithmeticOperator.class)
	void testSql(ArithmeticOperator op) {
		var sign = op.symbol;
		assertEquals("123" + sign + "456", op.sql(builder, new Integer[] {123, 456})); //integer
		assertEquals("123" + sign + "456", op.sql(builder, new Short[]   {123, 456})); // short
		assertEquals("123" + sign + "456", op.sql(builder, new Long[]    {123l, 456l})); // short
		assertEquals("1.3" + sign + "4.6", op.sql(builder, new Float[]   {1.3f, 4.6f})); // float
		assertEquals("1.3" + sign + "4.6", op.sql(builder, new Double[]  {1.3, 4.6})); // double
		assertEquals("123" + sign + "456", op.sql(builder, new Object[]  {new BigDecimal(123), new AtomicInteger(456)})); //variant
		assertEquals("col" + sign + "456", op.sql(builder, new Object[] {column("col"), 456}));  //variant 
		assertEquals("1.3" + sign + "col", op.sql(builder, new Object[] {1.3f, column("col")})); //variant
		assertEquals("col1" + sign + "col2", op.sql(builder, new Object[] {column("col1"), column("col2")})); //variant
	}
	
	@ParameterizedTest
	@EnumSource(ArithmeticOperator.class)
	void testSql_illegal_args(ArithmeticOperator op) {
		assertThrowsExactly(IllegalArgumentException.class, ()-> op.sql(builder, null)); //null arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> op.sql(builder, new Object[] {})); //empty arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> op.sql(builder, new Integer[] {123})); //1 numeric arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> op.sql(builder, new Long[] {1l,2l,3l})); //3 numeric arguments
	}

	@ParameterizedTest
	@EnumSource(ArithmeticOperator.class)
	void testSql_illegal_types(ArithmeticOperator op) {
		assertThrowsExactly(IllegalArgumentException.class, ()-> op.sql(builder, new Object[] {"a", "b"})); //2 string arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> op.sql(builder, new Object[] {'a', 'b'})); //2 char arguments
		assertThrowsExactly(IllegalArgumentException.class, ()-> op.sql(builder, new Object[] {from(now()), from(now())})); //2 timestamp arguments
	}
}
