package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.now;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.Column.allColumns;
import static org.usf.jquery.core.Column.constant;
import static org.usf.jquery.core.Utils.isEmpty;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.Stores;
import org.usf.jquery.core.Table;
import org.usf.jquery.core.View;

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
	
	private final ViewDecorator decorator; //cache
	private final Map<String, ColumnMetadata> columns; //key=identity
	@Getter
	private Instant lastUpdate;
	
	public ColumnMetadata columnMetadata(ColumnDecorator cd) {
		return columns.get(cd.identity());
	}
	
	protected void fetch(DatabaseMetaData metadata, String schema) throws SQLException {
		var view = decorator.view();
		if(!isEmpty(columns)) {
			try {
				var time = currentTimeMillis();
				log.info("scanning view '{}' metadata...", view);
				if(view instanceof Table tab) {
					fetchView(metadata, tab, schema);
				}
				else {
					fetch(metadata, view, schema);
				}
				lastUpdate = now();
				printViewColumnMap();
				log.trace("'{}' metadata scanned in {} ms", view, currentTimeMillis() - time);
			}
			catch(Exception e) {
				log.error("error while scanning '{}' metadata", view, e);
			}
		}
		else {
			log.warn("'{}' has no declared columns", view);
		}
	}
	
	void fetchView(DatabaseMetaData metadata, Table view, String schema) throws SQLException {
		schema = view.getSchemaOrElse(schema);
		try(var tm = metadata.getTables(null, schema, view.getName(), null)) {
			if(tm.next()) {
				if("TABLE".equals(tm.getString("TABLE_TYPE"))) { //!view
					try(var rs = metadata.getColumns(null, schema, view.getName(), null)){
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
							throw new IllegalArgumentException("no columns");
						}
					}
				}
				else {
					fetch(metadata, view, schema);
				}
			}
			else {
				throw new NoSuchElementException(view.toString() + " table or view not found");
			}
		}
	}

	void fetch(DatabaseMetaData metadata, View qr, String schema) throws SQLException {
		var query = new QueryComposer().columns(allColumns(qr)).criterias(constant(1).eq(constant(0))); //no data
		query.compose(Stores.getCurrentStore()).buildQuery(true).execute(rs->{
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
			return null;
		}, metadata.getConnection());
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
		return new NoSuchElementException("column(s) [" + join(", ", columns) + "] not found in " + decorator.view());
	}
}
