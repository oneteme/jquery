package org.usf.jquery.web;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.VIEW;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

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
	private final DatabaseMetadata metadata;
	//runtime scope
	private final Map<DBView, QueryView> overView = new HashMap<>();
	private final Map<String, TaggableColumn> declaredColumns = new HashMap<>();
	
	public ContextEnvironment(ContextEnvironment ctx) {
		this.database = ctx.database;
		this.views = new HashMap<>(ctx.views); //modifiable
		this.columns = new HashMap<>(ctx.columns); //modifiable
		this.dataSource = ctx.dataSource;
		this.schema = ctx.schema;
		this.metadata = ctx.metadata;
	}
	
	public Optional<ViewDecorator> lookupRegisteredView(String name) {
		return ofNullable(views.get(name));
	}
	
	public Optional<ColumnDecorator> lookupRegisteredColumn(String name) {
		return ofNullable(columns.get(name));
	}
	
	Optional<TaggableColumn> lookupDeclaredColumn(String name) {
		return ofNullable(declaredColumns.get(name));
	}
	
	void declareView(ViewDecorator view) { //additional request views
		views.compute(view.identity(), (k,v)-> {
			if(isNull(v)){
				return view;
			}
			throw resourceAlreadyExistsException(VIEW, k);
		});
	}
	
	TaggableColumn declareColumn(TaggableColumn col) {
		if(views.containsKey(col.tagname())) { //cannot overwrite registered views
			throw resourceAlreadyExistsException(VIEW, col.tagname());
		} //but can overwrite registered columns
		return declaredColumns.compute(col.tagname(), (k,v)-> {
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
		return metadata.getTables().computeIfAbsent(vd.identity(), key-> fn.apply(columns.values()));
	}
	
	ContextEnvironment bind() {
		if(nonNull(dataSource)) { //outer fetch
			for(var v : views.values()) {
				var meta = requireNonNull(v.metadata(), v.identity() + ".metadata");
				synchronized(meta) {
					try(var cnx = dataSource.getConnection()) {
						meta.fetch(cnx.getMetaData(), schema);
					}
					catch(SQLException | JQueryException e) {
						log.error("error while scanning database metadata", e);
					}
				}
			}
		}
		return this;
	}

	public static ContextEnvironment of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns) {
		return of(database, views, columns, null, null);
	}

	public static ContextEnvironment of(DatabaseDecorator database, 
			Collection<ViewDecorator> views,  Collection<ColumnDecorator> columns, DataSource ds) {
		return of(database, views, columns, ds, null);
	}
	
	public static ContextEnvironment of(DatabaseDecorator database, 
			Collection<ViewDecorator> views, Collection<ColumnDecorator> columns, DataSource ds, String schema) {
		requireLegalVariable(requireNonNull(database, "configuration.database").identity());
		return new ContextEnvironment(database,
				unmodifiableIdentityMap(views, ViewDecorator::identity, database.identity() + ".views"), 
				unmodifiableIdentityMap(columns, ColumnDecorator::identity, database.identity() + ".columns"),
				ds, schema, new DatabaseMetadata());
	}
	
	static <T> Map<String, T> unmodifiableIdentityMap(Collection<T> c, Function<T, String> fn, String msg){
		return unmodifiableMap(requireNonEmpty(c, msg).stream()
				.collect(toLinkedMap(fn.andThen(Validation::requireLegalVariable), identity())));
	}
	
	static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedMap(
    		Function<? super T, ? extends K> keyMapper, 
    		Function<? super T, ? extends U> valueMapper) {
		return toMap(keyMapper, valueMapper, 
				(v1, v2) -> {throw new IllegalStateException("!parallel");},
                LinkedHashMap::new);
	}
}