package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.AggregatFunction.SUM;
import static fr.enedis.teme.jquery.AggregatFunction.MAX;
import static fr.enedis.teme.jquery.Asserts.assertColNumber;
import static fr.enedis.teme.jquery.DBColumn.ofConstant;
import static fr.enedis.teme.jquery.DBColumn.ofReference;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		assertTrue(ofConstant(1234).plus(5678).isConstant());
		assertFalse(ofReference("cm1").minus(5678).isConstant());
		assertFalse(ofConstant(1234).multiply(ofReference("cm1")).isConstant());
		assertFalse(ofReference("cm1").divise(ofReference("cm2")).isConstant());
	}

	@Test
	void testIsAggregation() {
		assertTrue(SUM.of(ofReference("cm1")).plus(ofConstant(1234)).isAggregation());
		assertTrue(ofConstant(1234).minus(MAX.of(ofReference("cm2"))).isAggregation());
		assertFalse(ofConstant(1234).mode(5678).isAggregation());
		assertFalse(ofReference("cm1").pow(5678).isAggregation());
	}
	
}
