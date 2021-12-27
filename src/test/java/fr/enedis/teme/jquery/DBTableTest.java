package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DBTableTest {

	@Test
	void testToSql() {
		var tab = new GenericTable("table", null, null);
		assertEquals("table", tab.sql(null, null)); //null
		assertEquals("table_2000", tab.toSql(null, 2000, null));
		assertEquals("table", tab.sql("", null)); //empty
		assertEquals("table_2012", tab.toSql("", 2012, null));
		assertEquals("table", tab.sql(" ", null)); //blank
		assertEquals("table_2020", tab.toSql(" ", 2020, null));
		assertEquals("db.table", tab.sql("db", null));
		assertEquals("db.table_2022", tab.toSql("db", 2022, null));
	}

}
