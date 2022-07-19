package org.usf.jquery.core;

import static org.usf.jquery.core.Asserts.assertArrayType;
import static org.usf.jquery.core.Asserts.assertCallArray;
import static org.usf.jquery.core.Asserts.assertCallParameter;
import static org.usf.jquery.core.Asserts.assertCallString;
import static org.usf.jquery.core.Asserts.assertRequireAtLeastTwoParameter;
import static org.usf.jquery.core.Asserts.assertRequireOneParameter;
import static org.usf.jquery.core.Asserts.assertRequireTwoParameter;
import static org.usf.jquery.core.Asserts.assertStringType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StdComparatorTest  {
	
	@ParameterizedTest
	@EnumSource(value = StdComparator.class, names = {"EQ", "NE", "LT", "LE", "GT", "GE"})
	void testSql_sign(StdComparator op) {
		assertRequireTwoParameter(op);
		//accept all type
		assertCallParameter((o1,o2)-> o1+op.symbol+o2, op);
	}
	
	@ParameterizedTest
	@EnumSource(value = StdComparator.class, names = {"LIKE", "NOT_LIKE", "ILIKE", "NOT_ILIKE"})
	void testSql_like(StdComparator op) {
		assertRequireTwoParameter(op);
		assertStringType(op);
		assertCallString((o1,o2)-> o1+" "+op.name().replace('_', ' ')+" "+o2, op);
	}

	@ParameterizedTest
	@EnumSource(value = StdComparator.class, names = {"IN", "NOT_IN"})
	void testSql_in(StdComparator op) {
		assertRequireAtLeastTwoParameter(op);
		assertArrayType(op);
		assertCallArray((o1,o2)-> o1+" "+op.name().replace('_', ' ') + "(" + o2 + ")", op);
	}

	@ParameterizedTest
	@EnumSource(value = StdComparator.class, names = {"IS_NULL", "IS_NOT_NULL"})
	void testSql_null(StdComparator op) {
		assertRequireOneParameter(op);
		//accept all type
		assertCallParameter((o1,__)-> o1+" "+op.name().replace('_', ' '), (p, oper, __)-> op.sql(p, oper));
	}

}
