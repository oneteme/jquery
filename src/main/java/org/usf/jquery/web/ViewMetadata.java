package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
	private final Map<String, ColumnMetadata> columns; //key=identity
	@Getter
	private Instant lastUpdate;
	
	public ColumnMetadata columnMetadata(ColumnDecorator cd) {
		return columns.get(cd.identity());
	}
	
	final ViewMetadata fetch(DatabaseMetaData metadata, String schema) throws SQLException {
		if(!isEmpty(columns)) {
			try {
				var time = currentTimeMillis();
				log.info("scanning view '{}' metadata...", view);
				if(view instanceof TableView tab) {
					fetchView(metadata, tab, schema);
				}
				else {
					fetch(metadata, view, schema);
				}
				lastUpdate = now();
				log.trace("'{}' metadata scanned in {} ms", view, currentTimeMillis() - time);
				printViewColumnMap();
			}
			catch(Exception e) {
				log.error("error while scanning '{}' metadata", identity(), e);
			}
		}
		else {
			log.warn("'{}' has no declared columns", view);
		}
		return this;
	}
	
	void fetchView(DatabaseMetaData metadata, TableView view, String schema) throws SQLException {
		try(var rs = metadata.getColumns(null, view.getSchemaOrElse(schema), view.getName(), null)){
			if(rs.next()) {
				var db = reverseMapKeys(); //reverse key
				do {
					var cm = db.remove(rs.getString("COLUMN_NAME"));
					if(nonNull(cm)) {
						cm.update(
								rs.getInt("DATA_TYPE"), 
								rs.getInt("COLUMN_SIZE"), 
								rs.getInt("DECIMAL_DIGITS"));
					} // else undeclared column
				} while(rs.next());
				if(!db.isEmpty()) { //no such columns
					throw columnsNotFoundException(db.keySet());
				}
			}
			else {
				throw new NoSuchElementException(quote(view.toString()) + " table not found");
			}
		}
	}

	void fetch(DatabaseMetaData metadata, DBView qr, String schema) throws SQLException {
		var query = "SELECT * FROM " + qr.sql(parametrized(schema, emptyMap())) + " AS v0 WHERE 1=0"; // rows=0
		try(var ps = metadata.getConnection().prepareStatement(query);
			var rs = ps.executeQuery()){
			var db = reverseMapKeys();
			var meta = rs.getMetaData();
			for(var i=1; i<=meta.getColumnCount(); i++) {
				var cm = db.remove(meta.getColumnLabel(i)); //tag or name
				if(nonNull(cm)) {
					cm.update(meta.getColumnType(i), meta.getPrecision(i), meta.getScale(i));
				} // else undeclared column
			}
			if(!db.isEmpty()) { //no such columns
				throw columnsNotFoundException(db.keySet());
			}
		}
	}
	
	private Map<String, ColumnMetadata> reverseMapKeys(){ //key=columnName
		return columns.values().stream().collect(toMap(ColumnMetadata::getName, identity()));
	}
	
	void printViewColumnMap() {
		if(!columns.isEmpty() && log.isInfoEnabled()) {
			synchronized(LOG_LOCK) {
				var ptr = "|%-20s|%-15s|%-25s|%-20s|";
				var bar = format(ptr, "", "", "", "").replace("|", "+").replace(" ", "-");
				log.info(bar);
				log.info(format(ptr, "ID", "CLASS", "COLUMN", "TYPE"));
				log.info(bar);
				columns.entrySet().forEach(e-> 
				log.info(format(ptr, e.getKey(), e.getValue().toJavaType(), 
						e.getValue().getName(), e.getValue().toSqlType())));
				log.info(bar);
			}
		}
	}
	
	NoSuchElementException columnsNotFoundException(Set<String> columns) {
		return new NoSuchElementException("column(s) [" + join(", ", columns) + "] not found in " + view);
	}
}
