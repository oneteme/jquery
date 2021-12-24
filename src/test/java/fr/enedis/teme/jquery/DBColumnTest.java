package fr.enedis.teme.jquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DBColumnTest {
	
	private final DBColumn column = new GenericColumn("objField");
	private final DBTable table = new GenericTable("", Map.of(column, "tab_column"), null);

	@Test
	void testToSql() {
		assertEquals("tab_column", column.toSql(table));
		assertNotEquals(column.getMappedName(), column.toSql(table));
		assertThrows(NullPointerException.class, ()-> column.toSql(null));
	}

	@Test
	void testSqlAlias() {
		assertEquals("tab_column", column.sqlAlias(table));
		assertNotEquals(column.getMappedName(), column.sqlAlias(table));
		assertThrows(NullPointerException.class, ()-> column.sqlAlias(null));
	}

	@Test
	void testIsAggregated() {
		assertFalse(column.isAggregated());
	}

	@Test
	void testIsExpression() {
		assertFalse(column.isExpression());
	}

	@Test
	void testIsConstant() {
		assertFalse(column.isConstant());
	}

}
