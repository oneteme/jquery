package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.NoSuchResourceException.throwNoSuchTableException;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseConfiguration {
	
	private final DatabaseDecorator database;
	private final Map<String, ViewDecorator> views;
	private final Map<String, ColumnDecorator> columns;
	private final DataSource dataSource; //optional
	private final String schema; //optional
	
	public ViewDecorator getTable(String name) {
		var vd = views.get(name);
		if(nonNull(vd)) {
			return vd;
		}
		throw throwNoSuchTableException(name);
	}
	
	public static final DatabaseConfiguration of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns) {
		return of(database, views, columns, null, null);
	}

	public static final DatabaseConfiguration of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns, DataSource ds) {
		return of(database, views, columns, ds, null);
	}
	
	public static final DatabaseConfiguration of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns, DataSource ds, String schema) {
		return new DatabaseConfiguration(requireNonNull(database, "configuration.database"), 
				unmodifiableMap(requireNonEmpty(views, "configuration.views"), ViewDecorator::identity), 
				unmodifiableMap(requireNonEmpty(columns, "configuration.columns"), ColumnDecorator::identity),
				ds, schema);
	}
	
	static <T> Map<String, T> unmodifiableMap(Collection<T> c, Function<T, String> fn){
		return c.stream().collect(toUnmodifiableMap(fn, identity()));
	}
}