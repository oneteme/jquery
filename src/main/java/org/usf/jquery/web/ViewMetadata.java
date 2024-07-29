package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.now;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.usf.jquery.core.DBQuery;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.TableView;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@ToString
@Slf4j
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
public class ViewMetadata {
	
	private static final Object LOG_LOCK = new Object();
	
	private final DBView view; //cache
	private final Map<String, ColumnMetadata> columns; //empty!?
	@Getter
	private Instant lastUpdate;
	
	public ColumnMetadata columnMetada(ColumnDecorator cd) {
		return columns.get(cd.identity());
	}
	
	final ViewMetadata fetch(DatabaseMetaData metadata, String schema) throws SQLException {
		if(!isEmpty(columns)) {
			var time = currentTimeMillis();
			log.info("scanning table '{}' metadata...", view);
			if(view instanceof TableView tab) {
				fetch(metadata, tab, schema);
			}
			else if(view instanceof DBQuery query) {
				fetch(metadata, query);
			}
			else {
				throw new UnsupportedOperationException("unsupported view type " + view);
			}
			lastUpdate = now();
			log.info("metadata scanned in {} ms", currentTimeMillis() - time);
			printViewColumnMap();
		}
		return this;
	}
	
	void fetch(DatabaseMetaData metadata, TableView view, String schema) throws SQLException {
		try(var rs = metadata.getColumns(null, view.getSchemaOrElse(schema), view.getName(), null)){
			if(rs.next()) {
				var db = columns.values().stream().collect(toMap(m-> m.getColumn().getName(), identity()));
				do {
					var cm = db.remove(rs.getString("COLUMN_NAME"));
					if(nonNull(cm)) {
						cm.update(
								rs.getInt("DATA_TYPE"), 
								rs.getInt("COLUMN_SIZE"), 
								rs.getInt("DECIMAL_DIGITS"));
					} // else undeclared column
				} while(rs.next());
				if(!db.isEmpty()) {
					throw columnsNotFoundException(db);
				}
			}
			else {
				throw new NoSuchElementException(quote(view.toString()) + " table not found");
			}
		}
	}

	void fetch(DatabaseMetaData metadata, DBQuery qr) throws SQLException {
		var query = qr.sql(parametrized(new ArrayList<>()));
		try(var ps = metadata.getConnection().prepareStatement("SELECT * FROM(" + query + ") WHERE 1=0");
			var rs = ps.executeQuery()){
			var db = new HashMap<>(columns);
			var meta = rs.getMetaData();
			for(var i=1; i<=meta.getColumnCount(); i++) {
				var cm = db.remove(meta.getColumnLabel(i)); //tag or name
				if(nonNull(cm)) {
					cm.update(meta.getColumnType(i), meta.getColumnDisplaySize(i), meta.getPrecision(i));
				} // else undeclared column
			}
			if(!db.isEmpty()) {
				throw columnsNotFoundException(db);
			}
		}
	}
	
	void printViewColumnMap() {
		if(!columns.isEmpty() && log.isInfoEnabled()) {
			synchronized(LOG_LOCK) {
				var pattern = "|%-20s|%-15s|%-25s|%-20s|";
				var bar = format(pattern, "", "", "", "").replace("|", "+").replace(" ", "-");
				log.info(bar);
				log.info(format(pattern, "ID", "CLASS", "COLUMN", "TYPE"));
				log.info(bar);
				columns.entrySet().forEach(e-> 
				log.info(format(pattern, e.getKey(), e.getValue().toJavaType(), 
						e.getValue().getColumn(), e.getValue().toSqlType())));
				log.info(bar);
			}
		}
	}
	
	NoSuchElementException columnsNotFoundException(Map<String, ColumnMetadata> db) {
		return new NoSuchElementException("column(s) [" + join(", ", db.keySet()) + "] not found in " + view.toString());
	}
}
