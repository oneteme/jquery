package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.web.DatabaseMetadata.init;

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
	private DatabaseMetadata database; // by default
	private Resource resource;
	
	public static DatabaseScanner configure(String schema, @NonNull DataSource ds) {
		instance = new DatabaseScanner(new Configuration(schema, ds));
		return instance;
	}

	public static DatabaseScanner get(){
		return instance;
	}
	
	public static DatabaseMetadata database(){
		return requireNonNullElseGet(instance.database, DatabaseMetadata::emptyMetadata);
	}
	
	public static Resource resource(){
		return requireNonNullElseGet(instance.resource, Resource::emptyResource);
	}
		
	public DatabaseScanner register(
			@NonNull Collection<TableDecorator> tables, 
			@NonNull Collection<ColumnDecorator> columns){
		synchronized (mutex) {
			this.database = init(tables, columns);
			this.resource = Resource.init(tables, columns);
			return this;
		}
	}

	public void fetch() {
		if(isNull(database)) {
			log.warn("no resources !"); //full scan ? next release
			return;
		}
		synchronized (mutex) {
			var time = currentTimeMillis();
			log.info("Scanning database metadata...");
			try(var cn = config.getDataSource().getConnection()){
				var metadata = cn.getMetaData();
				for(var t : database.getTables().values()) {
					log.info("Scanning table '{}' metadata...", t.getTablename());
					t.fetch(metadata);
					logTableColumns(t.getColumns());
					if(t instanceof YearTableMetadata) {
						var yt = (YearTableMetadata) t;
						log.info("Scanning table '{}' revisions...", t.getTablename());
						yt.fetchRevisions(cn);
						logRevisions(yt.getRevisions());
					}
				}
				log.info("Completed metadata scan in {} ms", currentTimeMillis() - time);
			} catch (SQLException e) {
				log.error("Error while scanning database metadata", e);
			}
		}
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
