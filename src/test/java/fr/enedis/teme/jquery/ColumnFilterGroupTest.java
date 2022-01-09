package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericTable.tab1;
import static fr.enedis.teme.jquery.Helper.fieldValue;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.ParameterHolder.parametrized;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ColumnFilterGroupTest implements DataProvider {

	private final ParameterHolder STAT = addWithValue();
	private final ParameterHolder DYNC = parametrized();

	@ParameterizedTest
	@MethodSource("filterGroupCaseProvider")
	void testSql(ColumnFilterGroup filter, TableColumn[] columns, String[][] sql) {
		LogicalOperator op = (LogicalOperator) fieldValue("operator", filter);
		assertEquals(join(op, columns, sql[0]), filter.sql(tab1, DYNC));
		assertEquals(join(op, columns, sql[1]), filter.sql(tab1, STAT));
	}

	@Disabled
	@ParameterizedTest
	@MethodSource("filterGroupCaseProvider")
	void testToString(ColumnFilterGroup filter, DBColumn[] columns, String[][] sql) {
		LogicalOperator op = (LogicalOperator) fieldValue("operator", filter);
		assertEquals(join(op, sql[1]), filter.toString());
	}
	
	private static String join(LogicalOperator op, TableColumn[] columns, String[] sql) {
		return IntStream.range(0, columns.length)
			.mapToObj(i-> tab1.physicalColumnName(columns[i])+sql[i])
			.collect(joining(" " + op.name() + " "));
	}

	private static String join(LogicalOperator op, String[] sql) {
		return IntStream.range(0, sql.length)
			.mapToObj(i-> "${column}"+sql[i])
			.collect(joining(" " + op.name() + " "));
	}

}
