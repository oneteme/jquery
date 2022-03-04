package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.AggregatFunction.MAX;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.sql.DataSource;

import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.RequestQuery;
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
					var colum = unmodifiableMap(columnMetadata(e, names));
					var nRevs = names.stream().mapToInt(n-> parseInt(n.substring(n.length()-4))).sorted().toArray();
					var maxRe = nRevs.length > 0 ? maxRevision(e, nRevs[nRevs.length-1]) : null;
					meta.put(e.physicalName(), new TableMetadata(nRevs, maxRe, colum));
				}
			}
			else {
				var enums = (DBTable[]) t.getEnumConstants();
				for(var e : enums) {
					meta.put(e.physicalName(), new TableMetadata(unmodifiableMap(columnMetadata(e, e.physicalName()))));
				}
			}
		}
		instance.setTables(unmodifiableMap(meta));
	}
	
	private static List<String> tableNames(YearPartitionTable table) {
		
		try(ResultSet rs = ds.getConnection().getMetaData().getTables(null, null, table.physicalName()+"_20__", null)){
			List<String> nName = new LinkedList<>();
			while(rs.next()) {
				var tn = rs.getString("TABLE_NAME");
				if(tn.matches(table.physicalName() + "_20[0-9]{2}")) { // strict pattern
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
					log.warn(table.physicalName() + " => " + p.toString());
					log.warn(table.physicalName() + " => " + n.toString());
//					throw new RuntimeException("mismatch column description"); //break
					return n;
				}).orElse(emptyMap());
		}
		catch(RuntimeException e) {
			log.error("mismtach table definition", e);
			return emptyMap();
		}
	}
	
	private static Map<String, ColumnMetadata> columnMetadata(DBTable table, String tablename) {
		
		try(var cn = ds.getConnection()){
			var map = new HashMap<String, ColumnMetadata>(table.columns().length);
			try(var rs = cn.getMetaData().getColumns(null, null, tablename, null)){
				var columns = Stream.of(table.columns()).map(table::physicalColumnName).collect(toSet());
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
	
	private static int maxRevision(YearPartitionTable table, int year) {
		
		return new RequestQuery()
			.select(table.suffix(year+""), MAX.of(table.getRevisionColumn()).as("max"))
			.execute(o-> o.execute(ds, rs-> rs.getInt("max")));
	}
}
