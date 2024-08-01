package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.ConflictingResourceException.resourceAlreadyExistsException;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.VIEW;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JQueryException;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.TaggableColumn;
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
@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextEnvironment {
	
	private final DatabaseDecorator database;
	private final Map<String, ViewDecorator> views;
	private final Map<String, ColumnDecorator> columns;
	private final DataSource dataSource; //optional
	private final String schema; //optional
	private final DatabaseMetadata metadata = new DatabaseMetadata();
	//runtime scope
	private final Map<ViewDecorator, DBView> viewCache = new HashMap<>();
	private final Map<DBView, QueryView> overView = new HashMap<>();
	private final Map<String, TaggableColumn> declaredColumns = new HashMap<>();
	
	public ContextEnvironment(ContextEnvironment ctx) {
		this.database = ctx.database;
		this.views = new HashMap<>(ctx.views); //modifiable
		this.columns = new HashMap<>(ctx.columns); //modifiable
		this.dataSource = ctx.dataSource;
		this.schema = ctx.schema;
	}
	
	public Optional<ViewDecorator> lookupRegistredView(String name) {
		return ofNullable(views.get(name));
	}
	
	public Optional<ColumnDecorator> lookupRegistredColumn(String name) {
		return ofNullable(columns.get(name));
	}
	
	Optional<TaggableColumn> lookupDeclaredColumn(String name) {
		return ofNullable(declaredColumns.get(name));
	}
	
	public DBView getView(ViewDecorator vd, Supplier<DBView> supp) {
		return ofNullable(viewCache.get(vd)).orElseGet(supp);
	}
	
	void declareView(ViewDecorator view) {
		views.compute(view.identity(), (k,v)-> {
			if(isNull(v)){
				return view;
			}
			throw resourceAlreadyExistsException(VIEW, k);
		});
	}
	
	void declareColumn(TaggableColumn col) {
		declaredColumns.compute(col.tagname(), (k,v)-> {
			if(isNull(v)){
				return col;
			}
			throw resourceAlreadyExistsException(COLUMN, k);
		});
	}
	
	QueryView overView(DBView view, Supplier<QueryView> supp) {
		return overView.computeIfAbsent(view, k-> supp.get());
	}
	
	ViewMetadata computeTableMetadata(ViewDecorator vd, Function<Collection<ColumnDecorator>, ViewMetadata> fn) {
		var meta = metadata.getTables().computeIfAbsent(vd.identity(), key-> fn.apply(columns.values()));
		if(nonNull(dataSource)) { //outer fetch
			synchronized(meta) {
				if(isNull(meta.getLastUpdate())) {
					try(var cnx = dataSource.getConnection()) {
						meta.fetch(cnx.getMetaData(), schema);
					}
					catch(SQLException | JQueryException e) {
						log.error("error while scanning database metadata", e);
					}
				}
			}
		}
		return meta;
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
			Collection<ViewDecorator> views, Collection<ColumnDecorator> columns, DataSource ds, String schema) {
		requireLegalVariable(database.identity());
		return new ContextEnvironment(
				requireNonNull(database, "configuration.database"), 
				unmodifiableIdentityMap(requireNonEmpty(views, database.identity() + ".views"), ViewDecorator::identity), 
				unmodifiableIdentityMap(requireNonEmpty(columns, database.identity() + ".columns"), ColumnDecorator::identity),
				ds, schema);
	}
	
	static <T> Map<String, T> unmodifiableIdentityMap(Collection<T> c, Function<T, String> fn){
		return c.stream().collect(toUnmodifiableMap(fn.andThen(Validation::requireLegalVariable), identity()));
	}
}