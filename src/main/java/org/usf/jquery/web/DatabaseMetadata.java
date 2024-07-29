package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.now;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.Utils.isPresent;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.ColumnMetadata.columnMetadata;

import java.sql.SQLException;
import java.time.Instant;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.usf.jquery.core.Database;
import org.usf.jquery.core.ViewColumn;

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
@Getter(AccessLevel.PACKAGE)
public final class DatabaseMetadata {

	private final Object mutex = new Object();

	private final Map<String, ViewMetadata> tables = new HashMap<>(); //lazy loading
	@Getter
	private Instant lastUpdate;
	@Getter
	private Database type;

	public ViewMetadata viewMetadata(ViewDecorator td, Supplier<? extends ViewMetadata> supp){
		return tables.computeIfAbsent(td.identity(), id-> supp.get());
	}
	
	public void fetch(DataSource ds) {
		if(tables.isEmpty()) {
			log.warn("database resources not initialized"); //full scan ? next release
			return;
		}
		synchronized (mutex) { //thread safe
			var time = currentTimeMillis();
			log.info("scanning database metadata...");
			try(var cn = ds.getConnection()){
				var metadata = cn.getMetaData();
				type = Database.of(metadata.getDatabaseProductName()).orElse(null);
				for(var t : tables.values()) {
					log.info("Scanning table '{}' metadata...", t.getView());
					t.fetch(metadata, config.getSchema());
					if(t instanceof YearTableMetadata yt) {
						log.info("Scanning table '{}' revisions...", t.getView());
						yt.fetchRevisions(cn);
						logRevisions(yt.getRevisions());
					}
//					t.setLastUpdate(now());
				}
				lastUpdate = now();
				log.info("Completed metadata scan in {} ms", currentTimeMillis() - time);
			} catch (SQLException e) {
				log.error("Error while scanning database metadata", e);
			}
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

