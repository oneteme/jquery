package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.now;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.Utils.isPresent;

import java.sql.SQLException;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.usf.jquery.core.Database;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseMetadata {

	private final Object mutex = new Object();

	@Getter(AccessLevel.PACKAGE)	
	private final DataSource dataSource; //nullable if no sync
	private final Map<String, TableMetadata> tables; //empty if no sync
	@Getter
	private Instant lastUpdate;
	@Getter
	private Database type;

	public TableMetadata tableMetada(TableDecorator td, Supplier<TableMetadata> supp){
		return tables.computeIfAbsent(td.identity(), id-> supp.get());
	}
	
	public void fetch() {
		if(isNull(dataSource) || tables.isEmpty()) {
			log.warn("database resources not initialized"); //full scan ? next release
			return;
		}
		synchronized (mutex) { //thread safe
			var time = currentTimeMillis();
			log.info("Scanning database metadata...");
			try(var cn = dataSource.getConnection()){
				var metadata = cn.getMetaData();
				type = Database.of(metadata.getDatabaseProductName()).orElse(null);
				for(var t : tables.values()) {
					log.info("Scanning table '{}' metadata...", t.getView());
					t.fetch(metadata);
					logTableColumns(t.getColumns());
					if(t instanceof YearTableMetadata yt) {
						log.info("Scanning table '{}' revisions...", t.getView());
						yt.fetchRevisions(cn);
						logRevisions(yt.getRevisions());
					}
					t.setLastUpdate(now());
				}
				lastUpdate = now();
				log.info("Completed metadata scan in {} ms", currentTimeMillis() - time);
			} catch (SQLException e) {
				log.error("Error while scanning database metadata", e);
			}
		}
	}
	
	static void logTableColumns(Map<String, ColumnMetadata> map) {
		if(!map.isEmpty()) {
			var pattern = "|%-20s|%-15s|%-25s|%-20s|";
			var bar = format(pattern, "", "", "", "").replace("|", "+").replace(" ", "-");
			log.info(bar);
			log.info(format(pattern, "ID", "CLASS", "COLUMN", "TYPE"));
			log.info(bar);
			map.entrySet().forEach(e-> 
			log.info(format(pattern, e.getKey(), e.getValue().toJavaType(), 
					e.getValue().getColumn(), e.getValue().toSqlType())));
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

	static DatabaseMetadata create(DataSource ds, Collection<TableDecorator> tables, Collection<ColumnDecorator> columns) {
		return new DatabaseMetadata(ds, tables.stream()
				.collect(toUnmodifiableMap(TableDecorator::identity, t-> t.createMetadata(columns))));
	}
	
	static DatabaseMetadata emptyMetadata() {
		return new DatabaseMetadata(null, emptyMap());
	}
}

