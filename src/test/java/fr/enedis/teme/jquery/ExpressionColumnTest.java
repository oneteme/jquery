package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Asserts.assertColNumber;

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


}
