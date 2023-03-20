package org.usf.jquery.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usf.jquery.core.AggregatFunction.MAX;
import static org.usf.jquery.core.AggregatFunction.SUM;
import static org.usf.jquery.core.Asserts.assertColNumber;
import static org.usf.jquery.core.DBColumn.constant;
import static org.usf.jquery.core.DBColumn.column;

import org.junit.jupiter.api.Test;

class ExpressionColumnTest {

	@Test
	void testPlus(){
		assertColNumber((o1,o2)-> o1+"+"+o2, DBColumn::plus);
	}
	
	@Test
	void testMinus(){
		assertColNumber((o1,o2)-> o1+"-"+o2, DBColumn::minus);
	}

	@Test
	void testMultiply(){
		assertColNumber((o1,o2)-> o1+"*"+o2, DBColumn::multiply);
	}
	
	@Test
	void testDivise(){
		assertColNumber((o1,o2)-> o1+"/"+o2, DBColumn::divise);
	}
	
	@Test
	void testMode(){
		assertColNumber((o1,o2)-> o1+"%"+o2, DBColumn::mode);
	}

	@Test
	void testPow(){
		assertColNumber((o1,o2)-> o1+"^"+o2, DBColumn::pow);
	}

	@Test
	void testIsConstant() {
		assertTrue(constant(1234).plus(5678).isConstant());
		assertFalse(column("cm1").minus(5678).isConstant());
		assertFalse(constant(1234).multiply(column("cm1")).isConstant());
		assertFalse(column("cm1").divise(column("cm2")).isConstant());
	}

	@Test
	void testIsAggregation() {
		assertTrue(SUM.of(column("cm1")).plus(constant(1234)).isAggregation());
		assertTrue(constant(1234).minus(MAX.of(column("cm2"))).isAggregation());
		assertFalse(constant(1234).mode(5678).isAggregation());
		assertFalse(column("cm1").pow(5678).isAggregation());
	}
	
}
