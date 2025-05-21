package org.usf.jquery.web;

import static java.lang.ThreadLocal.withInitial;
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
import static org.usf.jquery.web.JQuery.apply;
import static org.usf.jquery.web.JQuery.getRequestParser;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JQueryException;
import org.usf.jquery.core.QueryComposer;

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
	private DatabaseMetadata metadata = NO_META; // optional
	//execution context
	private final Map<String, DBView> viewRefCache = new LinkedHashMap<>();
	private final ThreadLocal<List<QueryComposer>> stack = withInitial(ArrayList::new);
	
//	private final Map<String, TypedOperator> operators = null
//	private final Map<String, TypedComparator> comparators = null
	// securityManager
	
	public QueryComposer parse(String defaultView, String[] variables, Map<String, String[]> parameterMap) {
		return getRequestParser().parse(this, defaultView, variables, parameterMap);
	}
	
	public QueryComposer query(Consumer<QueryComposer> fn) {
		return apply(this, ctx-> { //no query
			var q = new QueryComposer(getMetadata().getType());
			var list = stack.get();
			if(list.add(q)) {
				try {
					fn.accept(q);
					return q;
				}
				finally {
					list.remove(q);
				}
			}
			throw new IllegalStateException();
		});
	}

	public QueryComposer currentQuery() {
		return getQuery(List::getLast);
	}
	
	public QueryComposer mainQuery() {
		return getQuery(List::getFirst);
	}
	
	private QueryComposer getQuery(Function<List<QueryComposer>, QueryComposer> fn) {
		var list = stack.get();
		if(!list.isEmpty()) {
			return fn.apply(list);
		}
		throw new IllegalStateException("no query in context");
	}
	
	/** assume unique instance of DBView */
	DBView cacheView(String name, Supplier<DBView> orElse) {
		return viewRefCache.computeIfAbsent(name, k-> orElse.get());
	}
	
	public Environment bind() {
		if(nonNull(dataSource)) {
			apply(this, ctx-> { //no query
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