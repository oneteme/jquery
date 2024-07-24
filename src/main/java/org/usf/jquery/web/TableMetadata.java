package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.JQueryContext.database;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.usf.jquery.core.DBQuery;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.QueryParameterBuilder;
import org.usf.jquery.core.TableView;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author u$f
 * 
 */
@ToString
@Getter(AccessLevel.PACKAGE)
public class TableMetadata {
	
	private DBView view; //nullable if query
	private Map<String, ColumnMetadata> columns;
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private Instant lastUpdate;

	public TableMetadata(DBView view, Map<String, ColumnMetadata> columns) {
		this.view = view;
		this.columns = columns;
	}
	
	public ColumnMetadata columnMetada(ColumnDecorator cd) {
		return columns.get(cd.identity());
	}

	public void fetch() throws SQLException { //individually table fetching
		try(var cn = database().getDataSource().getConnection()) {
			fetch(cn.getMetaData());
		}
	}
	
	void fetch(DatabaseMetaData metadata) throws SQLException {
		if(view instanceof TableView tv) {
			try(var rs = metadata.getColumns(null, tv.getSchema(), tv.getName(), null)){
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
						throw new NoSuchElementException("column(s) [" + join(", ", db.keySet()) + "] not found in " + view.toString());
					}
				}
				else {
					throw new NoSuchElementException(quote(view.toString()) + " table not found");
				}
			}
		}
		else if(view instanceof DBQuery qr) {
			var b = parametrized(new ArrayList<>());
			try(var ps = metadata.getConnection().prepareStatement("SELECT * FROM(" + qr.sql(b) + ") WHERE 1=0");
				var rs = ps.executeQuery()){
				var db = new HashMap<>(columns);
				var meta = rs.getMetaData();
				for(var i=1; i<=meta.getColumnCount(); i++) {
					var cm = db.remove(meta.getColumnName(i));
					if(nonNull(cm)) {
						cm.update(meta.getColumnType(i), meta.getColumnDisplaySize(i), meta.getPrecision(i));
					} // else undeclared column
				}
				if(!db.isEmpty()) {
					throw new NoSuchElementException("column(s) [" + join(", ", db.keySet()) + "] not found in " + view.toString());
				}
			}
			view = null; 
		}
	}
}
