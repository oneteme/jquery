package org.usf.jquery.web;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.core.Validation.requireNonEmpty;
import static org.usf.jquery.web.ColumnMetadata.columnMetadata;
import static org.usf.jquery.web.JQuery.context;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.usf.jquery.core.JQueryException;

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
public final class Environment {

	private static final DatabaseMetadata NO_META = new DatabaseMetadata(emptyMap());

	private static final Set<String> RESERVED_WORDS = Stream.of(Parameters.class.getDeclaredFields())
			.filter(f -> isStatic(f.getModifiers())).map(Field::getName).collect(toSet());

	private final DatabaseDecorator database;
	private final Map<String, ViewDecorator> views;
	private final Map<String, ColumnDecorator> columns;
	private final DataSource dataSource; // optional
	private final String schema; // optional
	private DatabaseMetadata metadata = NO_META; // required
//	private final Map<String, TypedOperator> operators = null
//	private final Map<String, TypedComparator> comparators = null
	// securityManager
	
	public Environment bind() {
		if(nonNull(dataSource)) {
			context(this, ctx-> {
				this.metadata = new DatabaseMetadata(toViewMetadata());
				try (var cnx = dataSource.getConnection()) {
					metadata.fetch(cnx.getMetaData(), schema);
					return metadata;
				} catch (SQLException e) {
					throw new JQueryException(e);
				}
			});
		} else {
			log.warn("no datasource configured, metadata not bound");
		}
		return this;
	}

	Map<String, ViewMetadata> toViewMetadata() {
		return views.values().stream().collect(toLinkedMap(v -> v.identity(),
				v -> requireNonNull(v.metadata(toColumnMetadata(v)), v.identity() + ".metadata")));
	}

	Map<String, ColumnMetadata> toColumnMetadata(ViewDecorator vd) {
		return columns.values().stream()
				.<Entry<String, ColumnMetadata>>mapMulti((cd, acc) -> ofNullable(vd.columnName(cd))
						.map(cn -> entry(cd.identity(), columnMetadata(cn, cd.type(vd)))).ifPresent(acc)) // view column only
				.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
	}

	public static Environment of(DatabaseDecorator database, Collection<ViewDecorator> views,
			Collection<ColumnDecorator> columns) {
		return of(database, views, columns, null, null);
	}

	public static Environment of(DatabaseDecorator database, Collection<ViewDecorator> views,
			Collection<ColumnDecorator> columns, DataSource ds) {
		return of(database, views, columns, ds, null);
	}

	public static Environment of(DatabaseDecorator database, Collection<ViewDecorator> views,
			Collection<ColumnDecorator> columns, DataSource ds, String schema) {
		assertIdentity(requireNonNull(database, "configuration.database").identity());
		return new Environment(database,
				unmodifiableIdentityMap(views, ViewDecorator::identity, database.identity() + ".views"), // preserve views order
				unmodifiableIdentityMap(columns, ColumnDecorator::identity, database.identity() + ".columns"), ds,
				schema);
	}

	static <T> Map<String, T> unmodifiableIdentityMap(Collection<T> c, Function<T, String> fn, String msg) {
		return unmodifiableMap(requireNonEmpty(c, msg).stream()
				.collect(toLinkedMap(fn.andThen(Environment::assertIdentity), identity())));
	}

	static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends U> valueMapper) {
		return toMap(keyMapper, valueMapper, 
				(v1, v2) -> { throw new IllegalStateException("!parallel");}, LinkedHashMap::new);
	}

	static String assertIdentity(String id) {
		if (!RESERVED_WORDS.contains(requireLegalVariable(id))) {
			return id;
		}
		throw new IllegalArgumentException("reserved word cannot be used as an identifier: " + id);
	}
}