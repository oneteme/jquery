package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.web.TableMetadata.EMPTY_REVISION;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.entry;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import fr.enedis.teme.jquery.DBTable;
import fr.enedis.teme.jquery.YearPartitionTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseScanner {
	
	private static DatabaseScanner instance = new DatabaseScanner(null, new DatabaseMetadata(emptyMap())); //!important : default init
	private final Object sync = new Object();

	private final Configuration config;
	private DatabaseMetadata metadata;
	
	@Getter
	List<ColumnDescriptor> columns;
	List<? extends DBTable> tables;

	DatabaseScanner(Configuration config, DatabaseMetadata metadata) {
		this(config);
		this.metadata = metadata;
	}
	
	public static DatabaseScanner configure(String schema, @NonNull DataSource ds) {
		return instance = new DatabaseScanner(new Configuration(schema, ds));
	}
	
	public static DatabaseScanner get() {
		return instance;
	}
	
	public DatabaseMetadata metadata(){
		return metadata;
	}
	
	List<ColumnDescriptor> columnDescriptors() {
		return columns;
	}
	
	public void fetch() {
		requireNonNull(config, "configuration not found");
		synchronized (sync) {
			var meta = new LinkedHashMap<String, TableMetadata>(); //avg
			for(var t : tables) {
				var declaredColumns = columns.stream()
						.filter(cd-> nonNull(t.dbColumnName(cd)))
						.collect(toMap(t::dbColumnName, identity()));
				if(t instanceof YearPartitionTable) {
					var e = (YearPartitionTable) t;
					var names = tableNames(e);
					var colum = columnMetadata(e, e.dbName()+"_20__", declaredColumns);
					var revs = names.isEmpty() || e.revisionColumn() == null ? null : yearMonthRevisions(e, names);
					meta.put(e.dbName(), new TableMetadata(revs, unmodifiableMap(colum)));
				}
				else {
					meta.put(t.dbName(), new TableMetadata(unmodifiableMap(columnMetadata(t, t.dbName(), declaredColumns))));
				}
			}
			this.metadata = new DatabaseMetadata(meta);
		}
	}
	
	private List<String> tableNames(YearPartitionTable table) {
		
		try(ResultSet rs = config.getDataSource().getConnection().getMetaData().getTables(null, null, table.dbName()+"_20__", null)){
			List<String> nName = new LinkedList<>();
			while(rs.next()) {
				var tn = rs.getString("TABLE_NAME");
				if(tn.matches(table.dbName() + "_20[0-9]{2}")) { // strict pattern
					nName.add(tn);
				}
			}
			log.info("{} : {}", table, nName);
			return nName;
		}
		catch(SQLException e) {
			log.error(table + " : cannot fetch table year revisions", e);
			return emptyList();
		}
	}
	
	private Map<String, ColumnMetadata> columnMetadata(DBTable table, String tablePattern, Map<String, ColumnDescriptor> declaredColumns) {
		
		try(var cn = config.getDataSource().getConnection()){
			try(var rs = cn.getMetaData().getColumns(null, null, tablePattern, null)){
				var def = new LinkedList<ColumnType>();
				while(rs.next()) {
					var col = rs.getString("COLUMN_NAME");
					var tab = rs.getString("TABLE_NAME");
					if(declaredColumns.containsKey(col) && tab.matches(table.dbName() + "_20[0-9]{2}")) {
						def.add(new ColumnType(tab, col, rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE")));
					}
				}
				return resolve(def, declaredColumns);
			}
		} catch (SQLException e) {
			log.error(table + " : cannot fetch table columns", e);
			return emptyMap();
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, ColumnMetadata> resolve(List<ColumnType> types, Map<String, ColumnDescriptor> declaredColumns){
		
		return Map.ofEntries(types.stream()
				.collect(groupingBy(ColumnType::getColumn)).entrySet()
				.stream().map(e->{
					var map = e.getValue().stream().collect(groupingBy(o-> hash(o.getLength(), o.getType())));
					ColumnType res = null;
					if(map.size() == 1) {
						res = e.getValue().get(0); 
					}
					else {
						log.warn("mismatch '{}' column definition : ", e.getKey());
						map.values().stream().forEach(o-> 
							log.warn("type:{}, length:{} => {}", o.get(0).getType(), o.get(0).getLength(), o.stream().map(ColumnType::getTable).collect(toList())));
						res = map.values().stream().max(comparing(Collection::size)).orElseThrow().get(0);
					}
					return entry(declaredColumns.get(res.getColumn()).value(), 
							new ColumnMetadata(res.getColumn(), res.getType(), res.getLength()));
				})
				.toArray(Entry[]::new));
	}
	
	private YearMonth[] yearMonthRevisions(YearPartitionTable table, List<String> tableNames) {
		
		try(var cn = config.getDataSource().getConnection()){
			var yearMonths = new LinkedList<YearMonth>();
			try(var ps = cn.createStatement()){
				var rc = table.revisionColumn().sql(null);
				var query = tableNames.stream()
						.map(tn-> "SELECT DISTINCT " + rc + ", " + tn.substring(tn.length()-4) + " FROM " + tn)
						.collect(joining("; "));
				log.debug(query);
				if(ps.execute(query)) {
					do {
						try(var rs = ps.getResultSet()){
							while(rs.next()) {
								yearMonths.add(YearMonth.of(rs.getInt(2), rs.getInt(1)));
							}
						}
					} while(ps.getMoreResults());
				}
			}
			return yearMonths.stream().sorted(reverseOrder()).toArray(YearMonth[]::new); //sort desc.
		}
		catch(SQLException e) {
			log.error(table + " : cannot fetch table month revisions", e);
			return EMPTY_REVISION;
		}
	}
	
	@Getter
	@RequiredArgsConstructor
	final class ColumnType {
		final String table;
		final String column;
		final int type;
		final int length;
	}
}
