package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.isBlank;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
