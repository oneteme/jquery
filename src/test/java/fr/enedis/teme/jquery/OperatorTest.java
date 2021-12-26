package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Operator.EQ;
import static fr.enedis.teme.jquery.Operator.GE;
import static fr.enedis.teme.jquery.Operator.GT;
import static fr.enedis.teme.jquery.Operator.LE;
import static fr.enedis.teme.jquery.Operator.LT;
import static fr.enedis.teme.jquery.Operator.*;
import static fr.enedis.teme.jquery.Operator.nParameter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OperatorTest {
	
	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"EQ", "NE", "LT", "LE", "GT", "GE"})
	void testSqlSign(Operator e) {
		var exp = map1.get(e);
		assertEquals(exp + "null", e.sql(null, false)); //null
		assertEquals(exp + "?", e.sql(null, true)); 
		assertEquals(exp + "33", e.sql(33, false)); //int
		assertEquals(exp + "?", e.sql(33, true)); 
		assertEquals(exp + "'33'", e.sql("33", false)); //string
		assertEquals(exp + "?", e.sql("33", true));
		assertEquals(exp + "'2020-01-01'", e.sql(LocalDate.of(2020, 1, 1), false));//date
		assertEquals(exp + "?", e.sql(LocalDate.of(2020, 1, 1), true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, false));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, true));
	}
	
	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"IS_NULL", "IS_NOT_NULL"})
	void testSqlNull(Operator e) {
		var exp = " " + map2.get(e);
		assertEquals(exp, e.sql(null, false)); //null
		assertEquals(exp, e.sql(null, true)); 
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, false)); //int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, true)); 
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", false)); //string
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), false));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, false));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, true));
	}
	
	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"LIKE", "NOT_LIKE"})
	void testSqlLike(Operator e) {
		var exp = " " + map3.get(e) + " ";
		assertThrows(IllegalArgumentException.class, ()-> e.sql(null, false));//null
		assertThrows(IllegalArgumentException.class, ()-> e.sql(null, true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, false));//int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, true));
		assertEquals(exp+"'33'", e.sql("33", false)); //string
		assertEquals(exp+"?", e.sql("33", true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), false));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, false));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, true));
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"IN", "NOT_IN"})
	void testSqlIn(Operator e) {
		var exp = " " + map4.get(e);
		assertThrows(NullPointerException.class, ()-> e.sql(null, false));//null
		assertThrows(NullPointerException.class, ()-> e.sql(null, true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, false));//int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", false)); //string
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), false));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), true));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, false));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, true));
		assertEquals(exp+"(1,2)", e.sql(new int[] {1,2}, false));//arr prim
		assertEquals(exp+"(?,?)", e.sql(new int[] {1,2}, true));
		assertEquals(exp+"('1','2','3')", e.sql(new String[] {"1","2","3"}, false));//arr string
		assertEquals(exp+"(?,?,?)", e.sql(new String[] {"1","2","3"}, true));
		assertEquals(exp+"(1.0)", e.sql(new Double[] {1.}, false));//arr string
		assertEquals(exp+"(?)", e.sql(new Double[] {1.}, true));
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"EQ", "NE", "LT", "LE", "GT", "GE"})
	void testToStringSign(Operator e) {
		assertEquals(map1.get(e), e.toString());
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"IS_NULL", "IS_NOT_NULL"})
	void testToStringNull(Operator e) {
		assertEquals(map2.get(e), e.toString());
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"LIKE", "NOT_LIKE"})
	void testToStringLike(Operator e) {
		assertEquals(map3.get(e), e.toString());
	}

	@Test
	void testNParameter() {
		assertEquals("?", nParameter(1));
		assertEquals("?,?", nParameter(2));
		assertEquals("?,?,?", nParameter(3));
	}

	final Map<Operator, String> map1 = Map.of(EQ, "=", NE, "<>", LT, "<", LE, "<=", GT, ">", GE, ">=");
	final Map<Operator, String> map2 = Map.of(IS_NULL, "IS NULL", IS_NOT_NULL, "IS NOT NULL");
	final Map<Operator, String> map3 = Map.of(LIKE, "LIKE", NOT_LIKE, "NOT LIKE");
	final Map<Operator, String> map4 = Map.of(IN, "IN", NOT_IN, "NOT IN");
	
}
