package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.web.DatabaseMetadata.EMPTY_DATABASE;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.sql.DataSource;

import lombok.AccessLevel;
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
	
	private static DatabaseScanner instance; //!important : default init
	
	private final Object mutex = new Object();
	
	private final Configuration config;
	private DatabaseMetadata database = EMPTY_DATABASE; // by default
	
	public static DatabaseScanner configure(String schema, @NonNull DataSource ds) {
		instance = new DatabaseScanner(new Configuration(schema, ds));
		return instance;
	}
	
	public static DatabaseScanner get() {
		return instance;
	}
	
	public static DatabaseMetadata database(){
		return get().database;
	}
	
	public static Configuration configuration(){
		return get().config;
	}
	
	public DatabaseScanner register(
			@NonNull Collection<TableDecorator> tables, 
			@NonNull Collection<ColumnDecorator> columns){
		synchronized (mutex) {
			this.database = new DatabaseMetadata(
					tables.stream().collect(toMap(TableDecorator::identity, DatabaseScanner::wrapp)), 
					columns.stream().collect(toMap(ColumnDecorator::identity, ColumnDecoratorWrapper::new)));
			return this;
		}
	}
	
	public void fetch() {
		if(database == EMPTY_DATABASE) {
			log.warn("no resources !"); //full scan ? next release
			return;
		}
		synchronized (mutex) {
			var time = currentTimeMillis();
			log.info("Scanning database metadata...");
			try(var cn = config.getDataSource().getConnection()){
				var metadata = cn.getMetaData();
				for(var t : database.tables()) {
					log.info("Scanning table '{}' metadata...", t.tableName());
					t.fetch(metadata);
					logTableColumns(t.tableMetadata.getColumns());
					if(t instanceof YearTableDecoratorWrapper) {
						var yt = (YearTableDecoratorWrapper) t;
						log.info("Scanning table '{}' revisions...", t.tableName());
						yt.fetchRevisions(cn);
						logRevisions(yt.availableRevisions());
					}
				}
				log.info("Completed metadata scan in {} ms", currentTimeMillis() - time);
			} catch (SQLException e) {
				log.error("Error while scanning database metadata", e);
			}
		}
	}
	
	private static TableDecoratorWrapper wrapp(TableDecorator t) {
		return t instanceof YearTableDecorator 
				? new YearTableDecoratorWrapper((YearTableDecorator)t) 
				: new TableDecoratorWrapper(t);
	}

	static void logTableColumns(Map<String, ColumnMetadata> map) {
		if(!map.isEmpty()) {
			var pattern = "|%-20s|%-40s|%-6s|%-12s|";
			var bar = format(pattern, "", "", "", "").replace("|", "+").replace(" ", "-");
			log.info(bar);
			log.info(format(pattern, "TAGNAME", "NAME", "TYPE", "LENGTH"));
			log.info(bar);
			map.entrySet().forEach(e-> 
			log.info(format(pattern, e.getKey(), e.getValue().getColumnName(), e.getValue().getDataType(), e.getValue().getDataSize())));
			log.info(bar);
		}
	}
	
	static void logRevisions(YearMonth[] revs) {
		if(isPresent(revs)) {
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
}
