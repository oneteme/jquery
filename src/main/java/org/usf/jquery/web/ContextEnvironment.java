package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.NoSuchResourceException.noSuchViewException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import org.usf.jquery.core.JQueryException;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.Validation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Getter
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextEnvironment {
	
	private final DatabaseDecorator database;
	private final Map<String, ViewDecorator> views;
	private final Map<String, ColumnDecorator> columns;
	private final DataSource dataSource; //optional
	private final String schema; //optional
	private final DatabaseMetadata metadata;
	
	public ContextEnvironment(ContextEnvironment ctx) {
		this.database = ctx.database;
		this.views = new HashMap<>(ctx.views); //modifiable
		this.columns = new HashMap<>(ctx.columns); //modifiable
		this.dataSource = ctx.dataSource;
		this.schema = ctx.schema;
		this.metadata = ctx.metadata;
	}
	
	public ViewDecorator lookupTable(String name) {
		var vd = views.get(name);
		if(nonNull(vd)) {
			return vd;
		}
		throw noSuchViewException(name);
	}
	
	ViewMetadata computeTableMetadata(ViewDecorator vd, Function<Collection<ColumnDecorator>, ViewMetadata> fn) {
		var meta = metadata.getTables().computeIfAbsent(vd.identity(), key-> fn.apply(columns.values()));
		if(nonNull(dataSource)) { //outer fetch
			synchronized(meta) {
				if(isNull(meta.getLastUpdate())) {
					fetch(meta);
				}
			}
		}
		return meta;
	}
	
	private void fetch(ViewMetadata metadata) {
		try(var cnx = dataSource.getConnection()) {
			metadata.fetch(cnx.getMetaData(), schema);
		}
		catch(SQLException | JQueryException e) {
			log.error("error while scanning database metadata", e);
		}
	}

	void registerQuery(QueryView query) {
		views.compute(query.id(), (k,v)-> {
			if(isNull(v)){
				return new QueryDecorator(query);
			}
			throw new IllegalStateException("already exists");
		});
		query.getQuery().getColumns()
		.stream().<ColumnDecorator>map(c-> c::tagname)
		.forEach(cd-> columns.compute(cd.identity(), (k,v)-> {
			if(isNull(v)) {
				return cd;
			}
			throw new IllegalStateException("already exists");
		}));
	}
	
	public static final ContextEnvironment of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns) {
		return of(database, views, columns, null, null);
	}

	public static final ContextEnvironment of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns, DataSource ds) {
		return of(database, views, columns, ds, null);
	}
	
	public static final ContextEnvironment of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns, DataSource ds, String schema) {
		requireLegalVariable(database.identity());
		return new ContextEnvironment(
				requireNonNull(database, "configuration.database"), 
				unmodifiableIdentityMap(requireNonEmpty(views, "configuration.views"), ViewDecorator::identity), 
				unmodifiableIdentityMap(requireNonEmpty(columns, "configuration.columns"), ColumnDecorator::identity),
				ds, schema, new DatabaseMetadata());
	}
	
	static <T> Map<String, T> unmodifiableIdentityMap(Collection<T> c, Function<T, String> fn){
		return c.stream().collect(toUnmodifiableMap(fn.andThen(Validation::requireLegalVariable), identity()));
	}
}