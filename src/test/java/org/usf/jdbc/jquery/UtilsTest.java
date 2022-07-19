package org.usf.jdbc.jquery;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.usf.jdbc.jquery.Utils.isBlank;
import static org.usf.jdbc.jquery.Utils.isEmpty;

import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void testIsEmptyArray() {
	 	assertTrue(isEmpty((Object[])null));
	 	assertTrue(isEmpty(new String[]{}));
	 	assertFalse(isEmpty(new Object[]{null}));
	 	assertFalse(isEmpty(new String[]{""}));
	}

	@Test
	void testIsBlank() {
	 	assertTrue(isBlank((String)null));
	 	assertTrue(isBlank(""));
	 	assertTrue(isBlank(" "));
	 	assertFalse(isBlank(" A "));
	}
}
