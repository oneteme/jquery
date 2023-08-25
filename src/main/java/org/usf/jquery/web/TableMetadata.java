package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.JQueryContext.database;
import static org.usf.jquery.web.ParsableJDBCType.typeOf;

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
	
	public Optional<ColumnMetadata> columnMetada(ColumnDecorator cd){
		return columns.containsKey(cd.identity()) ? empty() : Optional.of(columns.get(cd.identity()));
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
				var cn = rs.getString("COLUMN_NAME");
				if(dbMap.containsKey(cn)) {
					var meta = dbMap.remove(cn);
					meta.setDataType(typeOf(rs.getInt("DATA_TYPE")));
					meta.setDataSize(rs.getInt("COLUMN_SIZE"));
				}// else undeclared column
			} while(rs.next());
		}
		if(!dbMap.isEmpty()) {
			throw new NoSuchElementException("column(s) [" + join(", ", dbMap.keySet()) + "] not found in " + tablename);
		}
	}
	
	static TableMetadata tableMetadata(TableDecorator table, Collection<ColumnDecorator> columns) {
		var map = new LinkedHashMap<String, ColumnMetadata>();
		columns.stream().forEach(cd-> 
			table.columnName(cd).ifPresent(cn-> 
				map.put(cd.identity(), new ColumnMetadata(cn))));
		return new TableMetadata(table.tableName(), unmodifiableMap(map));
	}

	static TableMetadata emptyMetadata(TableDecorator table) {
		return new TableMetadata(table.tableName(), emptyMap());
	}
}
