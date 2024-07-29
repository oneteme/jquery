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

import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.Validation;

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
	
	public ViewDecorator getTable(String name) {
		var vd = views.get(name);
		if(nonNull(vd)) {
			return vd;
		}
		throw noSuchViewException(name);
	}
	
	ViewMetadata computeTableMetadata(ViewDecorator vd, Function<Collection<ColumnDecorator>, ViewMetadata> fn) {
		return metadata.getTables().computeIfAbsent(vd.identity(), key-> fn.apply(columns.values()));
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
	
	void fetch() {
		if(nonNull(dataSource)) {
			try(var cnx = dataSource.getConnection()) {
				views.values().stream().sorted(this::comparator).forEach(v->{ //parallel
					try {
						v.metadata().fetch(cnx.getMetaData(), schema);
					}
					catch (Exception e) {
						throw new WebException("error while fetching table metadata " + v.identity(), e);
					}
				});
			}
			catch(SQLException e) {
				throw new WebException("", e);
			}
		}
	}
	
	int comparator(ViewDecorator v1, ViewDecorator v2) {
		var n1 = database.viewName(v1);
		var n2 = database.viewName(v2);
		if((isNull(v1) && isNull(v2)) || (nonNull(n1) && nonNull(n2))) {
			return 0;
		}
		return nonNull(n1) ? 1 : -1;
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