package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.web.DatabaseMetadata.EMPTY_DATABASE;

import java.sql.SQLException;
import java.util.Collection;

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
			log.warn("pretty msg !"); //full scan ? next release
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

}
