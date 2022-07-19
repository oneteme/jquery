package org.usf.jquery.core;

import static org.usf.jquery.core.Asserts.assertColArray;
import static org.usf.jquery.core.Asserts.assertColParameter;
import static org.usf.jquery.core.Asserts.assertColString;

import org.junit.jupiter.api.Test;

class ColumnSingleFilterTest {
	
	@Test
	void testEqual(){
		assertColParameter((o1,o2)-> o1+"="+o2, DBColumn::equal);
	}
	
	@Test
	void testNotEqual(){
		assertColParameter((o1,o2)-> o1+"<>"+o2, DBColumn::notEqual);
	}

	@Test
	void testGreaterThan(){
		assertColParameter((o1,o2)-> o1+">"+o2, DBColumn::greaterThan);
	}
	
	@Test
	void testGreaterOrEqual(){
		assertColParameter((o1,o2)-> o1+">="+o2, DBColumn::greaterOrEqual);
	}

	@Test
	void testLessThan(){
		assertColParameter((o1,o2)-> o1+"<"+o2, DBColumn::lessThan);
	}
	
	@Test
	void testLessOrEqual(){
		assertColParameter((o1,o2)-> o1+"<="+o2, DBColumn::lessOrEqual);
	}

	@Test
	void testLike() {
		assertColString((o1,o2)-> o1+" LIKE "+o2, DBColumn::like);
	}

	@Test
	void testNotLike() {
		assertColString((o1,o2)-> o1+" NOT LIKE "+o2, DBColumn::notLike);
	}

	@Test
	void testILike() {
		assertColString((o1,o2)-> o1+" ILIKE "+o2, DBColumn::ilike);
	}

	@Test
	void testNotILike() {
		assertColString((o1,o2)-> o1+" NOT ILIKE "+o2, DBColumn::notILike);
	}
	
	@Test
	void testIn() {
		assertColArray((o1,o2)-> o1+" IN("+o2+")", DBColumn::in);
	}

	@Test
	void testNotIn() {
		assertColArray((o1,o2)-> o1+" NOT IN("+o2+")", DBColumn::notIn);
	}
	@Test
	void testIsNull() {
		assertColParameter((o1,__)-> o1+" IS NULL", (c,__)-> c.isNull());
	}

	@Test
	void testisNot() {
		assertColParameter((o1,__)-> o1+" IS NOT NULL", (c,__)-> c.isNotNull());
	}
}
