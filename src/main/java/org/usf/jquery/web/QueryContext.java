package org.usf.jquery.web;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.usf.jquery.core.ColumnProxy;
import org.usf.jquery.core.Comparator;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Operator;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public final class QueryContext {
	
	private final Environment environment;
	private final QueryComposer mainQuery;
	private final ViewDecorator defaultView;
	private final Map<String, ViewDecorator> views = new LinkedHashMap<>();
	private final Map<String, DBView> viewCache = new LinkedHashMap<>();
	
	public ViewDecorator declareView(ViewDecorator view) { //additional request views
		return views.compute(view.identity(), (k,v)-> {
			if(isNull(v)){
				return view;
			}
			throw resourceAlreadyExistsException(k);
		});
	}
	
	public Optional<NamedColumn> lookupDeclaredColumn(String name) {
		return mainQuery.getColumns().stream()
				.filter(ColumnProxy.class::isInstance) //tagged column only
				.filter(c-> name.equals(c.getTag()))
				.findAny();
	}
	
	public Optional<ViewDecorator> lookupRegisteredView(String name) { //+ declared
		return ofNullable(environment.getViews().get(name))
				.or(()-> ofNullable(views.get(name)));
	}
	
	public Optional<ColumnDecorator> lookupRegisteredColumn(String name) {
		return ofNullable(environment.getColumns().get(name));
	}

	public Optional<TypedOperator> lookupOperator(String op) {
		return lookup(Operator.class, TypedOperator.class, op);
	}
	
	public Optional<TypedComparator> lookupComparator(String cmp) {
		return lookup(Comparator.class, TypedComparator.class, cmp);
	}
	
	//assume unique instance of DBView
	DBView cacheView(String name, Supplier<DBView> orElse) {
		return viewCache.computeIfAbsent(name, k-> orElse.get());
	}
	
	static <T,U> Optional<U> lookup(Class<T> clazz, Class<U> type, String name) {
		try {
			var m = clazz.getMethod(name); //no parameter
			if(m.getReturnType() == type && m.getParameterCount() == 0 && isStatic(m.getModifiers()) && isPublic(m.getModifiers())) {
				return Optional.of(type.cast(m.invoke(null)));
			}
		} catch (Exception e) {/* do not throw exception */}
		return empty();
	}
}
