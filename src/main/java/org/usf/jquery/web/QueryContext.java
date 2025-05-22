package org.usf.jquery.web;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.ResourceAccessException.resourceAlreadyExistsException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.usf.jquery.core.ColumnProxy;
import org.usf.jquery.core.Comparator;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Operator;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
@Getter
@Setter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public final class QueryContext {
	
	private final ViewDecorator defaultView; //optional parse only
	private final Map<String, ViewDecorator> views = new LinkedHashMap<>();
	
	public ViewDecorator declareView(ViewDecorator view) { //additional request views
		return views.compute(view.identity(), (k,v)-> {
			if(isNull(v)){
				return view;
			}
			throw resourceAlreadyExistsException(k);
		});
	}
	
	public Optional<NamedColumn> lookupDeclaredColumn(String name) {
		var query = currentEnvironment().currentQuery();
		return query.getColumns().stream()
				.filter(ColumnProxy.class::isInstance) //tagged column only
				.filter(c-> name.equals(c.getTag()))
				.findAny();
	}
	
	public Optional<ViewDecorator> lookupRegisteredView(String name) { //+ declared
		return ofNullable(currentEnvironment().getViews().get(name))
				.or(()-> ofNullable(views.get(name)));
	}
	
	public Optional<ColumnDecorator> lookupRegisteredColumn(String name) {
		return ofNullable(currentEnvironment().getColumns().get(name));
	}

	public Optional<TypedOperator> lookupOperator(String op) {
		return lookup(Operator.class, TypedOperator.class, op);
	}
	
	public Optional<TypedComparator> lookupComparator(String cmp) {
		return lookup(Comparator.class, TypedComparator.class, cmp);
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
