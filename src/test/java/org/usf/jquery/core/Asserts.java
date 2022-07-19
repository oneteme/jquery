package org.usf.jquery.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.usf.jquery.core.DBColumn.ofConstant;
import static org.usf.jquery.core.DBColumn.ofReference;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.util.function.BiFunction;

final class Asserts {

	static void assertColParameter(BiFunction<Object, Object, String> format, BiFunction<DBColumn, Object, ? extends DBObject> fn) {
		assertColNumber(format, fn);
		assertColString(format, fn);
	}
	
	static void assertColNumber(BiFunction<Object, Object, String> format, BiFunction<DBColumn, Object, ? extends DBObject> fn) {
		assertEquals(format.apply("cm1", "cm2"), fn.apply(ofReference("cm1"), ofReference("cm2")).sql(addWithValue()));
		assertEquals(format.apply("cm1", 1234), fn.apply(ofReference("cm1"), 1234).sql(addWithValue()));
		assertEquals(format.apply(1234, "cm1"), fn.apply(ofConstant(1234), ofReference("cm1")).sql(addWithValue()));
		assertEquals(format.apply(1234, 5678), fn.apply(ofConstant(1234), 5678).sql(addWithValue()));
	}

	static void assertColString(BiFunction<Object, Object, String> format, BiFunction<DBColumn, Object, ? extends DBObject> fn) {
		assertEquals(format.apply("cm1", "cm2"), fn.apply(ofReference("cm1"), ofReference("cm2")).sql(addWithValue()));
		assertEquals(format.apply("cm1", "'ab'"), fn.apply(ofReference("cm1"), "ab").sql(addWithValue()));
		assertEquals(format.apply("'ab'", "cm1"), fn.apply(ofConstant("ab"), ofReference("cm1")).sql(addWithValue()));
		assertEquals(format.apply("'ab'", "'cd'"), fn.apply(ofConstant("ab"), "cd").sql(addWithValue()));
	}

	static void assertColArray(BiFunction<Object, Object, String> format, BiFunction<DBColumn, Object[], ? extends DBObject> fn) {
		assertEquals(format.apply("cm1", "'a','b','c'"), fn.apply(ofReference("cm1"), new Object[]{"a", "b", "c"}).sql(addWithValue()));
		assertEquals(format.apply("cm1", "1,2,3"), fn.apply(ofReference("cm1"), new Object[]{1,2,3}).sql(addWithValue()));
		assertEquals(format.apply("'z'", "'a','b','c'"), fn.apply(ofConstant("z"), new Object[]{"a", "b", "c"}).sql(addWithValue()));
		assertEquals(format.apply("9", "1,2,3"), fn.apply(ofConstant(9), new Object[]{1,2,3}).sql(addWithValue()));
	}
	

	static void assertCallParameter(BiFunction<Object, Object, String> format, DBCallable fn) {
		assertCallNumber(format, fn);
		assertCallString(format, fn);
	}
	
	static void assertCallNumber(BiFunction<Object, Object, String> format, DBCallable fn) {
		assertEquals(format.apply("cm1", "cm2"), fn.sql(addWithValue(), ofReference("cm1"), ofReference("cm2")));
		assertEquals(format.apply("cm1", 1234), fn.sql(addWithValue(), ofReference("cm1"), 1234));
		assertEquals(format.apply(1234, "cm1"), fn.sql(addWithValue(), ofConstant(1234), ofReference("cm1")));
		assertEquals(format.apply(1234, 5678), fn.sql(addWithValue(), ofConstant(1234), 5678));
	}
	
	static void assertCallString(BiFunction<Object, Object, String> format, DBCallable fn) {
		
		assertEquals(format.apply("cm1", "cm2"), fn.sql(addWithValue(), ofReference("cm1"), ofReference("cm2")));
		assertEquals(format.apply("cm1", "'ab'"), fn.sql(addWithValue(), ofReference("cm1"), "ab"));
		assertEquals(format.apply("'ab'", "cm1"), fn.sql(addWithValue(), ofConstant("ab"), ofReference("cm1")));
		assertEquals(format.apply("'ab'", "'cd'"), fn.sql(addWithValue(), ofConstant("ab"), "cd"));
	}
	


	static void assertNumberType(DBCallable fn) {
		assertNot(IllegalArgumentException.class, fn, "ab", "ba");
		assertNot(IllegalArgumentException.class, fn, ofReference("cm1"), "ba");
		assertNot(IllegalArgumentException.class, fn, "ab", ofReference("cm1"));
	}
	
	static void assertStringType(DBCallable fn) {

		assertNot(IllegalArgumentException.class, fn, 1234, 1234);
		assertNot(IllegalArgumentException.class, fn, ofReference("cm1"), 1234);
		assertNot(IllegalArgumentException.class, fn, 1234, ofReference("cm1"));
	}

	static void assertArrayType(DBCallable fn) {
		
	}

	static void assertCallArray(BiFunction<Object, Object, String> format, DBCallable fn) {
		assertEquals(format.apply("cm1", "'a','b','c'"), fn.sql(addWithValue(), ofReference("cm1"), new Object[]{"a", "b", "c"}));
		assertEquals(format.apply("cm1", "1,2,3"), fn.sql(addWithValue(), ofReference("cm1"), new Object[]{1,2,3}));
		assertEquals(format.apply("'z'", "'a','b','c'"), fn.sql(addWithValue(), ofConstant("z"), new Object[]{"a", "b", "c"}));
		assertEquals(format.apply("9", "1,2,3"), fn.sql(addWithValue(), ofConstant(9), new Object[]{1,2,3}));
	}

	
	static void assertRequireOneParameter(DBCallable call) {
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), null)); //null operand
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), ofReference("cm1"), 1234)); //2 args
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), ofReference("cm1"), "ab")); //2 args
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), 1234, "ab")); //2 args
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), "ab", 1234)); //2 args
	}

	static void assertRequireTwoParameter(DBCallable call) {
		assertRequireAtLeastTwoParameter(call);
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), ofReference("cm1"), 1234, "ab")); //3 args
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), ofReference("cm1"), "ab", 1234)); //3 args
	}

	static void assertRequireAtLeastTwoParameter(DBCallable call) {
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), null)); //null operand
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), ofReference("cm1"))); //1 args
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), 1234)); //1 args
		assertThrows(IllegalArgumentException.class, ()-> call.sql(addWithValue(), "ab")); //1 args
	}
	
	private static void assertNot(Class<? extends Exception> clazz, DBCallable op, Object oper, Object... values) {
		assertThrows(IllegalArgumentException.class, ()-> op.sql(addWithValue(), oper, values)); // 0 arg
	}
}
