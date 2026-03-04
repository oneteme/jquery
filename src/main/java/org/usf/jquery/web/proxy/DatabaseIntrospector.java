package org.usf.jquery.web.proxy;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.time.Instant.now;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Comparators.STD_COMPARTORS;
import static org.usf.jquery.core.JDBCType.fromDataType;
import static org.usf.jquery.core.Operators.STD_OPERATORS;
import static org.usf.jquery.core.ProductVendor.DEFAULT;
import static org.usf.jquery.core.ProductVendor.parseName;
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

import org.usf.jquery.core.H2Operators;
import org.usf.jquery.core.ProductVendor;
import org.usf.jquery.core.StoreMetadata;
import org.usf.jquery.core.TeradataOperators;

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
	
	static final StoreMetadata DEFAULT_META = new StoreMetadata(DEFAULT, null, STD_OPERATORS, STD_COMPARTORS);
	
	public static StoreMetadata storeMetadata(DataSource ds) {
		if(nonNull(ds)) {
			var type = fetchProduct(ds);
			var oper = switch (type) {
			case H2 -> new H2Operators();
			case TERADATA -> new TeradataOperators();
			default -> STD_OPERATORS;
			};
			return new StoreMetadata(type, ds, oper, STD_COMPARTORS);
		}
		return DEFAULT_META;
	}
	
	public static ProductVendor fetchProduct(DataSource ds) {
		try(var conn = ds.getConnection()) {
			var name = conn.getMetaData().getDatabaseProductName();
			return parseName(name);
		}
		catch(Exception e) {
			log.warn("error while fetching database product name", e);
			return null;
		}
	}
	
	public static DatasetMetadata datasetMetadata(String schema, String dataset, Set<String> columns, DataSource ds) {
		Map<String, ColumnMetadata> map = new HashMap<>();
		DatasetType type = null;
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
			for(var col : columns) {
				map.putIfAbsent(col, null); //if column metadata is missing, put null value to preserve column declaration 
			}
		}
		catch (Exception e) {
			log.error("error while fetching metadata for dataset '{}'", dataset, e);
		}
		return new DatasetMetadata(dataset, type, unmodifiableMap(map), now());
	}
	
	static DatasetType datasetType(DatabaseMetaData meta, String schema, String table) {
		try(var tm = meta.getTables(null, schema, table, new String[] {"TABLE", "VIEW"})) {
			if(tm.next()) {
				return "TABLE".equals(tm.getString("TABLE_TYPE")) ? TABLE : VIEW;
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
