package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.concat;
import static fr.enedis.teme.jquery.Utils.isBlank;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Utils.nArgs;
import static fr.enedis.teme.jquery.Utils.sqlString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.function.IntFunction;

import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void testIsEmptyIntArray() {
	 	assertTrue(isEmpty((int[])null));
	 	assertTrue(isEmpty(new int[]{}));
	 	assertFalse(isEmpty(new int[]{1}));
	}

	@Test
	void testIsEmptyDoubleArray() {
	 	assertTrue(isEmpty((double[])null));
	 	assertTrue(isEmpty(new double[]{}));
	 	assertFalse(isEmpty(new double[]{.1}));
	}

	@Test
	void testIsEmptyTArray() {
	 	assertTrue(isEmpty((String[])null));
	 	assertTrue(isEmpty(new String[]{}));
	 	assertFalse(isEmpty(new String[]{""}));
	}

	@Test
	void testIsEmptyString() {
	 	assertTrue(isEmpty((String)null));
	 	assertTrue(isEmpty(""));
	 	assertFalse(isEmpty(" "));
	 	assertFalse(isEmpty("A"));
	}

	@Test
	void testIsBlank() {
	 	assertTrue(isBlank((String)null));
	 	assertTrue(isBlank(""));
	 	assertTrue(isBlank(" "));
	 	assertFalse(isBlank("A"));
	}

	@Test
	void testNArgs() {
		assertEquals("(?)", nArgs(1));
		assertEquals("(?,?)", nArgs(2));
		assertEquals("(?,?,?,?,?)", nArgs(5));
		assertThrows(IllegalArgumentException.class, ()-> nArgs(0));
		assertThrows(IllegalArgumentException.class, ()-> nArgs(-1));
	}

	@Test
	void testConcatDBColumnArrayDBColumnArray() {
		//fail("Not yet implemented");
	}

	@Test
	void testConcatDBFilterArrayDBFilterArray() {
		//fail("Not yet implemented");
	}

	@Test
	void testConcatTArrayTArrayIntFunctionOfT() {
		IntFunction<String[]> fn = String[]::new;
		assertArrayEquals(null, concat((String[])null, (String[])null, fn));
		assertArrayEquals(array(), concat(array(), null, fn));
		assertArrayEquals(array(), concat(null, array(), fn));
		assertArrayEquals(array(), concat(array(), new String[]{}, fn));
		assertArrayEquals(array(""), concat(array(""), null, fn));
		assertArrayEquals(array(""), concat(null, array(""), fn));
		String[] a1 = array("1","2"), a2 = array("3","4","5");
		assertArrayEquals(array("1","2","3","4","5"), concat(a1, a2, fn));
		assertThrows(NullPointerException.class, ()-> concat(a1, a2, null));
	}

	@Test
	void testSqlString() {
		assertEquals("'0'", sqlString(0));
		assertEquals("'A'", sqlString("A"));
		assertEquals("'2020-01-01'", sqlString(LocalDate.of(2020, 1, 1)));
	}

	private static String[] array(String... arr) {
		return arr;
	}
}
