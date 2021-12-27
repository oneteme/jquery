package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.staticSql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class OperatorTest implements ExpressionProvider {
	
	private final ParameterHolder STAT = staticSql();
	private final ParameterHolder DYNC = staticSql();
	private final LocalDate date = LocalDate.now();
	
	@ParameterizedTest
	@MethodSource("caseProvider")
	void testSql(Operator op, String sql, Object arg, ParameterHolder ph) {
		assertEquals(sql, op.sql(arg, ph));
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class)
	void testToString(Operator e) {
		assertEquals(enumMap.get(e), e.toString());
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"EQ", "NE", "LT", "LE", "GT", "GE"})
	void testSqlSign(Operator e) {
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {0}, STAT));//primitive array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {0}, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {"abc"}, STAT));//String array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {"abc"}, DYNC));
	}
	
	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"LIKE", "NOT_LIKE"})
	void testSqlLike(Operator e) {
		assertThrows(NullPointerException.class, ()-> e.sql(null, STAT));//null
		assertThrows(NullPointerException.class, ()-> e.sql(null, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(1234, STAT));//int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(1234, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(date, STAT));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(date, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {0}, STAT));//primitive array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {0}, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {"abc"}, STAT));//String array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {"abc"}, DYNC));
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"IN", "NOT_IN"})
	void testSqlIn(Operator e) {
		assertThrows(NullPointerException.class, ()-> e.sql(null, STAT));//null
		assertThrows(NullPointerException.class, ()-> e.sql(null, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(1234, STAT));//int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(1234, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql("abc", STAT));//string
		assertThrows(IllegalArgumentException.class, ()-> e.sql("abc", DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(date, STAT));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(date, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, STAT));//primitive array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {}, STAT));//String array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {}, DYNC));
	}

	@ParameterizedTest()
	@EnumSource(value = Operator.class, names = {"IS_NULL", "IS_NOT_NULL"})
	void testSqlNull(Operator e) {
		assertThrows(IllegalArgumentException.class, ()-> e.sql(1234, STAT));//int
		assertThrows(IllegalArgumentException.class, ()-> e.sql(1234, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql("abc", STAT));//string
		assertThrows(IllegalArgumentException.class, ()-> e.sql("abc", DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(date, STAT));//date
		assertThrows(IllegalArgumentException.class, ()-> e.sql(date, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, STAT));//primitive array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new int[] {}, DYNC));
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {}, STAT));//String array
		assertThrows(IllegalArgumentException.class, ()-> e.sql(new String[] {}, DYNC));
	}

}
