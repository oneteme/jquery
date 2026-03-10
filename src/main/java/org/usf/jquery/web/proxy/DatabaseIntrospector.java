package org.usf.jquery.web.proxy;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.time.Instant.now;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.fromDataType;
import static org.usf.jquery.core.Provider.DEFAULT;
import static org.usf.jquery.core.Provider.parseName;
import static org.usf.jquery.web.proxy.DatasetType.TABLE;
import static org.usf.jquery.web.proxy.DatasetType.VIEW;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.usf.jquery.core.Dialect;
import org.usf.jquery.core.H2Dialect;
import org.usf.jquery.core.Provider;
import org.usf.jquery.core.TeradataDialect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseIntrospector {
	
	public static Dialect storeDialect(DataSource ds) {
		var provider = fetchProduct(ds);
		return switch (provider) {
		case H2 -> new H2Dialect();
		case TERADATA -> new TeradataDialect();
		default -> new Dialect(provider);
		};
	}
	
	public static Provider fetchProduct(DataSource ds) {
		try(var cnx = ds.getConnection()) {
			var name = cnx.getMetaData().getDatabaseProductName();
			if(nonNull(name)) {
				return parseName(name);
			}
		}
		catch(Exception e) {
			log.warn("error while fetching database product name", e);
		}
		return DEFAULT;
	}
	
	public static DatasetMetadata datasetMetadata(String schema, String dataset, Set<String> columns, DataSource ds) {
		DatasetType type = null;
		Map<String, ColumnMetadata> map = new HashMap<>();
		if(nonNull(ds)) {
			try(var cnx = ds.getConnection()) {
				columns = insensitiveTreeSet(columns);
				var meta = cnx.getMetaData();
				type = datasetType(meta, schema, dataset);
				if(nonNull(type)) {
					map = fetchTableMetadata(schema, dataset, columns, meta);
					if(map.isEmpty()) {
						map = fetchViewMetadata(schema, dataset, columns, cnx);
					}
				}
			}
			catch (SQLException e) {
				log.error("error while fetching metadata for dataset '{}'", dataset, e);
			}
			catch (Exception e) {
				log.error("unexpected error while fetching metadata for dataset '{}'", dataset, e);
				type = null;
				map.clear();
			}
		}
		//if column metadata is missing, put null value to preserve column declaration 
		for(var col : columns) {
			map.putIfAbsent(col, null);
		}
		return new DatasetMetadata(dataset, type, unmodifiableMap(map), now());
	}
	
	static DatasetType datasetType(DatabaseMetaData meta, String schema, String table) {
		try(var tm = meta.getTables(null, schema, table, new String[] {"TABLE", "VIEW"})) {
			if(tm.next()) {
				var type = tm.getString("TABLE_TYPE"); //'BASE TABLE' for H2
				if(nonNull(type)) {
					return type.contains("TABLE") ? TABLE : VIEW;
				}
			}
		}
		catch(Exception e) {
			log.error("error while fetching table '{}' metadata", table, e);
		}
		return null;
	}

	static Map<String, ColumnMetadata> fetchTableMetadata(String schema, String table, Set<String> columns, DatabaseMetaData meta) {
		var map = new HashMap<String, ColumnMetadata>();
		try(var tm = meta.getColumns(null, schema, table, null)) {
			while(tm.next()) {
				var name = tm.getString("COLUMN_NAME");
				if(columns.contains(name)) {
					map.put(name, new ColumnMetadata(name, 
						fromDataType(tm.getInt("DATA_TYPE")).orElse(null), 
						tm.getInt("COLUMN_SIZE"), 
						tm.getInt("DECIMAL_DIGITS")));
				}
			}
		}
		catch(SQLException e) {
			log.error("error while fetching column metadata for table '{}'", table, e);
		}
		return map;
	}
	
	static Map<String, ColumnMetadata> fetchViewMetadata(String schema, String table, Set<String> columns, Connection cnx) {
		var map = new HashMap<String, ColumnMetadata>();
		var name = isNull(schema) || schema.isEmpty() ? table : schema + '.' + table;
		try (var ps = cnx.createStatement(); 
				var rs = ps.executeQuery("SELECT * FROM " + name + " WHERE 1=0")) { //fetch metadata only
			var meta = rs.getMetaData();
			for(int i=1; i<=meta.getColumnCount(); i++) {
				name = meta.getColumnName(i);
				if(columns.contains(name)) {
					map.put(name, new ColumnMetadata(name, 
						fromDataType(meta.getColumnType(i)).orElse(null), 
						meta.getPrecision(i), 
						meta.getScale(i)));
				}
			}
		}
		catch(SQLException e) {
			log.error("error while fetching column metadata for table '{}'", table, e);
		}
		return map;
	}
	
	static TreeSet<String> insensitiveTreeSet(Set<String> columns) {
		var set = new TreeSet<String>(CASE_INSENSITIVE_ORDER);
		set.addAll(columns);
		return set;
	}
}
