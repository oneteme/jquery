package fr.enedis.teme.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.sql.DataSource;

import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.YearPartitionTable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseScanner {

	private static DataSource ds;
	private static List<Class<? extends Enum<? extends DBTable>>> types;
	private static DatabaseMetaData instance = new DatabaseMetaData();

	public static void configure(@NonNull DataSource ds, @NonNull List<Class<? extends Enum<? extends DBTable>>> types) {
		DatabaseScanner.ds = ds;
		DatabaseScanner.types = types;
		fetch();
	}
	
	public static DatabaseMetaData metadata() {
		return instance;
	}
	
	public static void fetch() {
		var meta = new HashMap<String, TableMetadata>(types.size() * 10); //avg
		for(var t : types) {
			if(YearPartitionTable.class.isAssignableFrom(t)) {
				var enums = (YearPartitionTable[]) t.getEnumConstants();
				for(var e : enums) {
					var names = tableNames(e);
					var nRevs = names.stream().mapToInt(n-> parseInt(n.substring(n.length()-4))).toArray();
					meta.put(e.getTableName(), new TableMetadata(nRevs, unmodifiableMap(columnMetadata(e, names))));
				}
			}
			else {
				var enums = (DBTable[]) t.getEnumConstants();
				for(var e : enums) {
					meta.put(e.getTableName(), new TableMetadata(null, unmodifiableMap(columnMetadata(e, e.getTableName()))));
				}
			}
		}
		instance.setTables(unmodifiableMap(meta));
	}
	
	private static List<String> tableNames(YearPartitionTable table) {
		
		try(ResultSet rs = ds.getConnection().getMetaData().getTables(null, null, table.getTableName()+"_20__", null)){
			List<String> nName = new LinkedList<>();
			while(rs.next()) {
				var tn = rs.getString("TABLE_NAME");
				if(tn.matches(table.getTableName() + "_20[0-9]{2}")) { // strict pattern
					nName.add(tn);
				}
			}
			return nName;
		}
		catch(SQLException e) {
			log.error(table + " : cannot fetch table revision", e);
			return emptyList();
		}
	}
	
	private static Map<String, ColumnMetadata> columnMetadata(YearPartitionTable table, List<String> names) {
		
		try {
			return names.stream().map(n-> columnMetadata(table, n))
				.reduce((p, n)-> {
					if(p.equals(n)) {
						return n;
					}
					throw new RuntimeException("mismtach partition"); //break
				}).orElse(null);
		}
		catch(RuntimeException e) {
			return emptyMap();
		}
	}
	
	private static Map<String, ColumnMetadata> columnMetadata(DBTable table, String tablename) {
		
		try(var cn = ds.getConnection()){
			var map = new HashMap<String, ColumnMetadata>(table.columns().length);
			try(var rs = cn.getMetaData().getColumns(null, null, tablename, null)){
				var columns = Stream.of(table.columns()).map(table::dbColumnName).collect(toSet());
				while(rs.next()) {
					var name = rs.getString("COLUMN_NAME");
					if(columns.contains(name)) {
						map.put(name, new ColumnMetadata(
								rs.getInt("DATA_TYPE"), 
								rs.getInt("COLUMN_SIZE")));
					}
				}
			}
			return map;
		} catch (SQLException e) {
			log.error(table + " : cannot fetch table columns", e);
			return emptyMap();
		}
	}
	
	//can check 
	
	@SuppressWarnings("unused")
	private static YearMonth[] tableMonths(YearPartitionTable table, List<String> tableNames) {
		
		var rc = table.getRevisionColumn().sql(table, null);
		var query = tableNames.stream()
			.map(tn-> "SELECT DISTINCT " + rc + ", " + tn.substring(tn.length()-4) + " FROM " + tn)
			.collect(joining("; "));
		var months = new LinkedList<YearMonth>();
		try(var cn = ds.getConnection()){
			try(var ps = cn.createStatement()){
				log.info(query);
				boolean res = ps.execute(query);
				if(res) {
					do {
						try(var rs = ps.getResultSet()){
							while(rs.next()) {
								months.add(YearMonth.of(rs.getInt(2), rs.getInt(1)));
							}
						}
						res = ps.getMoreResults();
					} while(res);
				}
			}
		}
		catch(SQLException e) {
			log.warn(table + " : cannot fetch table month revision", e);
		}
		return months.stream().sorted(reverseOrder()).toArray(YearMonth[]::new); //sort desc.
	}
}
