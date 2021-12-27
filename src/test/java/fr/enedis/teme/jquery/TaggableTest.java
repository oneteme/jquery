package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.GenericTable.tab1;
import static fr.enedis.teme.jquery.Taggable.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TaggableTest {

	@Test
	void testGenericTag() {
		assertThrows(NullPointerException.class, ()-> genericTag(null, null, null));
		assertThrows(NullPointerException.class, ()-> genericTag(null, null, tab1));
		assertThrows(NullPointerException.class, ()-> genericTag(null, c1, null));
		assertEquals("someId", genericTag(null, c1, tab1));
		assertEquals("testSomeId", genericTag("test", c1, tab1));
	}
	
	@ParameterizedTest
	@CsvSource({"test,test", "test,TEST", "testCase,test_case", 
		"testCase,test_CASE", "testCase,TEST_CASE"})
	void testSnakeToCamelCase(String expected, String input) {
		assertEquals(expected, snakeToCamelCase(input));
		assertEquals(expected, snakeToCamelCase(input));
		assertEquals(expected, snakeToCamelCase(input));
		assertEquals(expected, snakeToCamelCase(input));
		assertEquals(expected, snakeToCamelCase(input));
		assertEquals(expected, snakeToCamelCase(input));
	}

}
