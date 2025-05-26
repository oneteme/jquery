package org.usf.jquery.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.HashMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequestParameterResolver2Test {
	
	final StrictRequestParameterResolver res = new StrictRequestParameterResolver();
	final String key = "dummy";
	
	// test bool params
	
	@ParameterizedTest
	@MethodSource("testAppendParams_bool_success")
	void testAppendParams_bool_success(Boolean params, String[] expectedValue) { // can remove "key" parameter
		var map = new HashMap<String, String[]>();	
		res.appendParam(map, key, params);
//		assertEquals(1, map.size());
		assertArrayEquals(expectedValue, map.get(key));
	}
	
	static Stream<Arguments> testAppendParams_bool_success() {
		return Stream.of(    
				arguments(true, new String[] {"true"}),
				arguments(false, null));
	}
	
	// test arr params
	
	@ParameterizedTest
	@MethodSource("testAppendParams_arr_success")
	void testAppendParams_arr_success(String[] params, String[] expectedValue) { // can remove "key" parameter
		var map = new HashMap<String, String[]>();	
		res.appendParam(map, key, params);
//		assertEquals(1, map.size());
		assertArrayEquals(expectedValue, map.get(key));
	}
	
	static Stream<Arguments> testAppendParams_arr_success() {
		return Stream.of(    
				arguments(new String[] {"id","customer","contact"}, new String[] {"id","customer","contact"}),
				arguments(null, null));
	}
	
	// test int params
	
	@ParameterizedTest
	@MethodSource("testAppendParams_int_success")
	void testAppendParams_int_success(int params, String[] expectedValue) { // can remove "key" parameter
		var map = new HashMap<String, String[]>();	
		res.appendParam(map, key, params);
//		assertEquals(1, map.size());
		assertArrayEquals(expectedValue, map.get(key));
	}
	
	static Stream<Arguments> testAppendParams_int_success() {
		return Stream.of(    
				arguments(0, new String[] {"0"}),
				arguments(1, new String[] {"1"}),
				arguments(-1, null),
				arguments(-5, null));
	}
	
	// Filters Success
	
	static Stream<Arguments> testAppendFilters_success() {
		var filters = new String[] {"x=y","k","z="};
		return Stream.of(
				arguments(filters, "k", null),
				arguments(filters, "z", null),
				arguments(filters, "x", "y")
				);
	}

	@ParameterizedTest
	@MethodSource("testAppendFilters_success")
	void testAppendFilters_success(String[] params, String key,String expectedValue) {
		var map = new HashMap<String, String[]>();
		res.appendParams(map, params);
		assertArrayEquals(expectedValue != null ? new String[]{expectedValue}:null, map.get(key));
	}
	
	// Filters Exceptions
	
	static Stream<Arguments> testAppendFilters_fail() {
		String nullFilterKey = "filter key cannot be null : ";
		String incorrectFilter = "incorrect filter format : ";
		return Stream.of(
				arguments(new String[] {"=x=y"}, incorrectFilter + "'=x=y'"),
				arguments(new String[] {"x=y,z="}, incorrectFilter + "'x=y,z='"),
				arguments(new String[] {"x=val1,val2=,val3"}, incorrectFilter + "'x=val1,val2=,val3'"),
				arguments(new String[] {"=x"}, nullFilterKey + "'=x'")
				);
	}
	
	@ParameterizedTest
	@MethodSource("testAppendFilters_fail")
	void testAppendFilters_fail(String[] filter, String msg) {
		var map = new HashMap<String, String[]>();
		assertThrowsMessage(msg, ()-> {
			res.appendParams(map, filter);
		});
	}
	
	
	// Utils
	
	private void assertThrowsMessage(String msg, Executable code) {
		var ex = assertThrows(IllegalArgumentException.class, code);
	    assertEquals(msg, ex.getMessage());
	}
}
