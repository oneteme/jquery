package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Operator.*;
import static fr.enedis.teme.jquery.ParameterHolder.parametredSql;
import static fr.enedis.teme.jquery.ParameterHolder.staticSql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OperatorTest {
	
	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"EQ", "NE", "LT", "LE", "GT", "GE"})
	void testSqlSign(Operator e) {
		var exp = map1.get(e);
		var pph = parametredSql();
		assertEquals(exp + "null", e.sql(null, staticSql())); //null
		assertEquals(exp + "?", e.sql(null, pph)); 
		assertEquals(exp + "33", e.sql(33, staticSql())); //int
		assertEquals(exp + "?", e.sql(33, pph)); 
		assertEquals(exp + "'33'", e.sql("33", staticSql())); //string
		assertEquals(exp + "?", e.sql("33", pph));
		assertEquals(exp + "'2020-01-01'", e.sql(LocalDate.of(2020, 1, 1), staticSql()));//date
		assertEquals(exp + "?", e.sql(LocalDate.of(2020, 1, 1), pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, staticSql()));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, pph));
	}
	
	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"IS_NULL", "IS_NOT_NULL"})
	void testSqlNull(Operator e) {
		var exp = " " + map2.get(e);
		var pph = parametredSql();
		assertEquals(exp, e.sql(null, staticSql())); //null
		assertEquals(exp, e.sql(null, pph)); 
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, staticSql())); //int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, pph)); 
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", staticSql())); //string
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), staticSql()));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, staticSql()));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, pph));
	}
	
	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"LIKE", "NOT_LIKE"})
	void testSqlLike(Operator e) {
		var exp = " " + map3.get(e) + " ";
		var pph = parametredSql();
		assertThrows(NullPointerException.class, ()-> e.sql(null, staticSql()));//null
		assertThrows(NullPointerException.class, ()-> e.sql(null, pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, staticSql()));//int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, pph));
		assertEquals(exp+"'33'", e.sql("33", staticSql())); //string
		assertEquals(exp+"?", e.sql("33", pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), staticSql()));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, staticSql()));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, pph));
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"IN", "NOT_IN"})
	void testSqlIn(Operator e) {
		var exp = " " + map4.get(e);
		var pph = parametredSql();
		assertThrows(NullPointerException.class, ()-> e.sql(null, staticSql()));//null
		assertThrows(NullPointerException.class, ()-> e.sql(null, pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, staticSql()));//int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(33, pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", staticSql())); //string
		assertThrows(IllegalArgumentException.class, ()-> e.sql("33", pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), staticSql()));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(LocalDate.of(2020, 1, 1), pph));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, staticSql()));//arr
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, pph));
		assertEquals(exp+"(1,2)", e.sql(new int[] {1,2}, staticSql()));//arr prim
		assertEquals(exp+"(?,?)", e.sql(new int[] {1,2}, pph));
		assertEquals(exp+"('1','2','3')", e.sql(new String[] {"1","2","3"}, staticSql()));//arr string
		assertEquals(exp+"(?,?,?)", e.sql(new String[] {"1","2","3"}, pph));
		assertEquals(exp+"(1.0)", e.sql(new Double[] {1.}, staticSql()));//arr string
		assertEquals(exp+"(?)", e.sql(new Double[] {1.}, pph));
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


	final Map<Operator, String> map1 = Map.of(EQ, "=", NE, "<>", LT, "<", LE, "<=", GT, ">", GE, ">=");
	final Map<Operator, String> map2 = Map.of(IS_NULL, "IS NULL", IS_NOT_NULL, "IS NOT NULL");
	final Map<Operator, String> map3 = Map.of(LIKE, "LIKE", NOT_LIKE, "NOT LIKE");
	final Map<Operator, String> map4 = Map.of(IN, "IN", NOT_IN, "NOT IN");
	
}
