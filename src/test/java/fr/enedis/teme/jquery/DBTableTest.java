package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DBTableTest {

	@Test
	void testToSql() {
		var tab = new GenericTable("table", null, null);
		assertEquals("table", tab.sql(null, null)); //null
		assertEquals("table", tab.sql("", null)); //empty
		assertEquals("table", tab.sql(" ", null)); //blank
		assertEquals("db.table", tab.sql("db", null));
	}

}
