package org.usf.jquery.web;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.web.DatabaseMetadata.create;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchColumnException;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchTableException;

import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JQueryContext {
	
	private static JQueryContext instance; //!important : default init

	private final Map<String, TableDecorator> tables;
	private final Map<String, ColumnDecorator> columns;
	private DatabaseMetadata database;
	
	public static JQueryContext context(){
		return requireNonNull(instance, ()-> "jquery context not initialized");
	}
	
	public static DatabaseMetadata database(){
		return requireNonNullElseGet(context().database, DatabaseMetadata::emptyMetadata);  // by default
	}

	public static JQueryContext register(
			@NonNull Collection<TableDecorator> tables, 
			@NonNull Collection<ColumnDecorator> columns){
		instance = new JQueryContext(
				tables.stream().collect(toUnmodifiableMap(TableDecorator::identity, identity())),
				columns.stream().collect(toUnmodifiableMap(ColumnDecorator::identity, identity())));
		return instance;
	}
	
	public DatabaseMetadata bind(DataSource ds){
		database = create(ds, tables.values(), columns.values());
		return database;
	}
	
	public boolean isDeclaredTable(String id) {
		return tables.containsKey(id);
	}

	public TableDecorator getTable(String value) {
		return ofNullable(tables.get(value)).
				orElseThrow(()-> throwNoSuchTableException(value));
	}
	
	public boolean isDeclaredColumn(String id) {
		return columns.containsKey(id);
	}

	public ColumnDecorator getColumn(String value) {
		return ofNullable(columns.get(value))
				.orElseThrow(()-> throwNoSuchColumnException(value));
	}
	
	public Collection<TableDecorator> tables(){
		return tables.values();
	}

	public Collection<ColumnDecorator> columns(){
		return columns.values();
	}
}
