package org.usf.jquery.web;

import static java.lang.String.format;
import static java.time.Month.DECEMBER;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Map.entry;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.web.YearTableDecoratorWrapper.EMPTY_REVISION;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseScanner {
	
	private static DatabaseScanner instance = new DatabaseScanner(null, new DatabaseMetadata(emptyMap())); //!important : default init
	private final Object sync = new Object();

	private final Configuration config;
	
	@Deprecated
	List<ColumnDecorator> columns = emptyList();
	List<? extends TableDecorator> tables = emptyList();
	
	Map<String, ColumnDecorator> columnMap;
	Map<String, TableDecorator> tableMap;

	DatabaseScanner(Configuration config, DatabaseMetadata metadata) {
		this(config);
		this.metadata = metadata;
	}
	
	public static DatabaseScanner configure(String schema, @NonNull DataSource ds) {
		instance = new DatabaseScanner(new Configuration(schema, ds));
		return instance;
	}
	
	public static DatabaseScanner get() {
		return instance;
	}
	
	public DatabaseMetadata metadata(){
		return metadata;
	}
	
	public DatabaseScanner register(List<? extends TableDecorator> tables, List<ColumnDecorator> columns){
		
		this.tables = unmodifiableList(tables);
		this.columns = unmodifiableList(columns);
		return this;
	}
	
	List<ColumnDecorator> columnDescriptors() {
		return columns;
	}
	
	List<? extends TableDecorator> tableDescriptors() {
		return tables;
	}
	
	public void fetch() {
		requireNonNull(config, "configuration not found");
		synchronized (sync) {
			for(var t : tableMap.values()) {
				var declaredColumns = columns.stream()
						.filter(cd-> {
							try {
								t.columnName(cd);
							} catch (Exception e) {
								return false;
							}
							return true;
						})
						.collect(toMap(t::columnName, identity()));
				if(t instanceof YearTableDecorator) {
					var e = (YearTableDecorator) t;
					var names = tableNames(e);
					var colum = columnMetadata(e, e.tableName()+"_20__", declaredColumns);
					YearMonth[] revs = names.isEmpty() ? null : yearMonthRevisions(e, names);
					meta.put(e, new YearTableDecoratorWrapper(unmodifiableMap(colum), revs));
				}
				else {
					meta.put(t, new TableMetadata(unmodifiableMap(columnMetadata(t, t.tableName(), declaredColumns))));
				}
			}
		}
	}
	
	private List<String> tableNames(YearTableDecorator table) {
		
		log.info("Scanning '{}' table year partitions...", table);
		try(ResultSet rs = config.getDataSource().getConnection().getMetaData().getTables(null, null, table.tableName()+"_20__", null)){
			List<String> nName = new LinkedList<>();
			while(rs.next()) {
				var tn = rs.getString("TABLE_NAME");
				if(tn.matches(table.tableName() + "_20[0-9]{2}")) { // strict pattern
					nName.add(tn);
				}
			}
			log.info("[{}]", nName.stream().map(t-> t.substring(t.length()-4)).collect(joining(", ")));
			return nName;
		}
		catch(SQLException e) {
			log.error(table + " : cannot fetch table year revisions", e);
			return emptyList();
		}
	}
	
	private Map<String, ColumnDecorator> columnMetadata(TableDecorator table, String tablePattern, Map<String, ColumnDecorator> declaredColumns) {

		log.info("Scanning '{}' table columns...", table);
		try(var cn = config.getDataSource().getConnection()){
			try(var rs = cn.getMetaData().getColumns(null, null, tablePattern, null)){
				var def = new LinkedList<ColumnType>();
				while(rs.next()) {
					var col = rs.getString("COLUMN_NAME");
					var tab = rs.getString("TABLE_NAME");
					if(declaredColumns.containsKey(col)) { //regex check table name
						def.add(new ColumnType(tab, col, rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE")));
					}
				}
				var cols = resolve(def, declaredColumns);
				logTableColumns(cols);
				return cols;
			}
		} catch (SQLException e) {
			log.error(table + " : cannot fetch table columns", e);
			return emptyMap();
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<ColumnDecorator, ColumnMetadata> resolve(List<ColumnType> types, Map<String, ColumnDecorator> declaredColumns){
		
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
					return entry(declaredColumns.get(res.getColumn()).identity(), 
							new ColumnMetadata(res.getColumn(), res.getType(), res.getLength()));
				})
				.toArray(Entry[]::new));
	}
	
	private YearMonth[] yearMonthRevisions(YearTableDecorator table, List<String> tableNames) {
		
		if(table.revisionColumn() == null) {
			return tableNames.stream()
					.map(n-> n.substring(n.length()-4))
					.map(Year::parse)
					.sorted(reverseOrder())
					.map(y-> y.atMonth(DECEMBER))
					.toArray(YearMonth[]::new);
		}
		log.info("Scanning '{}' table month revisions...", table);
		try(var cn = config.getDataSource().getConnection()){
			var yearMonths = new LinkedList<YearMonth>();
			try(var ps = cn.createStatement()){
				var rc = table.columnName(table.revisionColumn());
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
			var revs = yearMonths.stream().sorted(reverseOrder()).toArray(YearMonth[]::new); //sort desc.
			logRevisions(revs);
			return revs;
		}
		catch(SQLException e) {
			log.error(table + " : cannot fetch table month revisions", e);
			return EMPTY_REVISION;
		}
	}
	
	private static void logTableColumns(Map<String, ColumnDecorator> map) {
		if(!map.isEmpty()) {
			var pattern = "|%-20s|%-40s|%-6s|%-12s|";
			var bar = format(pattern, "", "", "", "").replace("|", "+").replace(" ", "-");
			log.info(bar);
			log.info(format(pattern, "TAGNAME", "NAME", "TYPE", "LENGTH"));
			log.info(bar);
			map.entrySet().forEach(e-> 
			log.info(format(pattern, e.getValue().identity(), e.getKey(), e.getValue().dataType(), e.getValue().dataSize())));
			log.info(bar);
		}
	}

	private static void logRevisions(YearMonth[] revs) {
		
		if(revs.length > 0) {
			var pattern = "|%-5s|%-40s|";
			var bar = format(pattern, "", "").replace("|", "+").replace(" ", "-");
			var map = Stream.of(revs).collect(groupingBy(YearMonth::getYear));
			log.info(bar);
			log.info(format(pattern, "YEAR", "MONTHS"));
			log.info(bar);
			map.entrySet().stream().sorted(comparing(Entry::getKey)).forEach(e-> 
			log.info(format(pattern, e.getKey(), e.getValue().stream().map(o-> o.getMonthValue() + "").collect(joining(", ")))));
			log.info(bar);
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
