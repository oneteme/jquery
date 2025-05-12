package org.usf.jquery.web;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.usf.jquery.core.JQueryException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@Builder()
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextEnvironment {
	
	private static final Set<String> RESERVED_WORDS = Stream.of(Parameters.class.getDeclaredFields())
			.filter(f-> isStatic(f.getModifiers()))
			.map(Field::getName)
			.collect(toSet());
	
	private final DatabaseDecorator database;
	private final Map<String, ViewDecorator> views;
	private final Map<String, ColumnDecorator> columns;
	private final DataSource dataSource; //optional
	private final String schema; //optional
	private final DatabaseMetadata metadata;
	//operation, comparators, ..
	
	public Optional<ViewDecorator> lookupRegisteredView(String name) { //+ declared
		return ofNullable(views.get(name));
	}

	ViewDecorator declareView(ViewDecorator view) { //additional request views
		return views.compute(view.identity(), (k,v)-> {
			if(isNull(v)){
				return view;
			}
			throw resourceAlreadyExistsException(k);
		});
	}

	ViewMetadata computeTableMetadata(ViewDecorator vd, Function<Collection<ColumnDecorator>, ViewMetadata> fn) {
		return metadata.getTables().computeIfAbsent(vd.identity(), key-> fn.apply(columns.values()));
	}
	
	ContextEnvironment bind() {
		if(nonNull(dataSource)) {
			try(var cnx = dataSource.getConnection()) {
				var cm = cnx.getMetaData();
				metadata.fetch(cm);
				for(var v : views.values()) {
					var meta = requireNonNull(v.metadata(), v.identity() + ".metadata");
					synchronized(meta) {
						meta.fetch(cm, schema);
					}
				}
			}
			catch (SQLException e) {
				throw new JQueryException(e);
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
		assertIdentity(requireNonNull(database, "configuration.database").identity());
		return new ContextEnvironment(database,
				unmodifiableIdentityMap(views, ViewDecorator::identity, database.identity() + ".views"), //preserve views order
				unmodifiableIdentityMap(columns, ColumnDecorator::identity, database.identity() + ".columns"),
				ds, schema, new DatabaseMetadata());
	}
	
	static <T> Map<String, T> unmodifiableIdentityMap(Collection<T> c, Function<T, String> fn, String msg){
		return unmodifiableMap(requireNonEmpty(c, msg).stream()
				.collect(toLinkedMap(fn.andThen(ContextEnvironment::assertIdentity), identity())));
	}
	
	static <T, K, U> Collector<T, ?, Map<K,U>> toLinkedMap(
    		Function<? super T, ? extends K> keyMapper, 
    		Function<? super T, ? extends U> valueMapper) {
		return toMap(keyMapper, valueMapper, 
				(v1, v2) -> {throw new IllegalStateException("!parallel");},
                LinkedHashMap::new);
	}
	
	static String assertIdentity(String id) {
		if(!RESERVED_WORDS.contains(requireLegalVariable(id))){
			return id;
		}
		throw new IllegalArgumentException("reserved word cannot be used as an identifier: " + id);
	}
}