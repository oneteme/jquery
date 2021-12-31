package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.Taggable.prefix;
import static fr.enedis.teme.jquery.Taggable.snakeToCamelCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TaggableTest {

	@Test
	void testGenericTag() {
		assertThrows(NullPointerException.class, ()-> prefix(null, null));
		assertThrows(NullPointerException.class, ()-> prefix("", null));
		assertEquals("somecode", prefix(null, c1)); //TODO camel case bug
		assertEquals("testSomecode", prefix("test", c1));
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
