package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.JDBCType.OTHER;
import static org.usf.jquery.core.JDBCType.fromDataType;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.JQueryContext.database;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author u$f
 * 
 */
@ToString
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TableMetadata {
	
	private final String tablename;
	private final Map<String, ColumnMetadata> columns;
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private Instant lastUpdate;
	
	public Optional<ColumnMetadata> columnMetada(ColumnDecorator cd) {
		return Optional.ofNullable(columns.get(cd.identity()));
	}

	public void fetch() throws SQLException { //individually table fetching
		try(var cn = database().getDataSource().getConnection()) {
			fetch(cn.getMetaData());
		}
	}
	
	void fetch(DatabaseMetaData metadata) throws SQLException {
		var dbMap = columns.values().stream().collect(toMap(ColumnMetadata::getColumnName, identity()));
		try(var rs = metadata.getColumns(null, null, tablename, null)){
			if(!rs.next()) {
				throw new NoSuchElementException(quote(tablename) + " table not found");
			}
			do {
				var meta = dbMap.remove(rs.getString("COLUMN_NAME"));
				if(nonNull(meta)) {
					meta.setDataType(fromDataType(rs.getInt("DATA_TYPE")).orElse(OTHER));
					meta.setDataSize(rs.getInt("COLUMN_SIZE"));
					meta.setPrecision(rs.getInt("DECIMAL_DIGITS"));
				} // else undeclared column
			} while(rs.next());
		}
		if(!dbMap.isEmpty()) {
			throw new NoSuchElementException("column(s) [" + join(", ", dbMap.keySet()) + "] not found in " + tablename);
		}
	}

	static TableMetadata emptyMetadata(TableDecorator table) {
		return tableMetadata(table, emptyList());
	}
	
	static TableMetadata tableMetadata(TableDecorator table, Collection<ColumnDecorator> columns) {
		return new TableMetadata(table.tableName(), declaredColumns(table, columns));
	}
	
	static Map<String, ColumnMetadata> declaredColumns(TableDecorator table, Collection<ColumnDecorator> columns){
		return unmodifiableMap(columns.stream().reduce(new LinkedHashMap<String, ColumnMetadata>(), (m, cd)-> {
			table.columnName(cd).map(ColumnMetadata::new).ifPresent(cm-> m.put(cd.identity(), cm));
			return m;
		}, (m1, m2)-> m1));
	}
}
