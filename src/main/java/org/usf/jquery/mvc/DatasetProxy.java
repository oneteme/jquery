package org.usf.jquery.mvc;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.usf.jquery.core.QueryPart.toSQL;
import static org.usf.jquery.mvc.Bind.BindType.REF;
import static org.usf.jquery.mvc.DatabaseIntrospector.datasetMetadata;
import static org.usf.jquery.mvc.MethodUtils.getMethod;

import java.lang.reflect.Method;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.JoinGroup;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.Table;
import org.usf.jquery.core.View;
import org.usf.jquery.core.ViewColumn;

/**
 * 
 * @author u$f
 *
 */
final class DatasetProxy extends ResourceProxy {

	private final View view;
	private final DatasetMetadata metadata;
	
	DatasetProxy(View view, Map<String, Method> exposedMethods, Map<Method, Object> resourcesCache, DatasetMetadata metadata) {
		super(exposedMethods, resourcesCache);
		this.view = view;
		this.metadata = metadata;
	}

	@Override
	Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
		var res = super.invokeDefaultMethod(proxy, method, args);
		if(res instanceof Column col) {
			var rsr = method.getAnnotation(Expose.class); //exposed resource
			if(nonNull(rsr) && !rsr.alias().isEmpty()) {
				res = col.as(rsr.alias());
			}
			var typ = method.getAnnotation(Typed.class); //abstract & default method
			if(nonNull(typ)) {
				res = col.as(typ.value());
			}
		}
		return res;
	}	
	
	@Override
	Object invokeAbstractMethod(Object proxy, Method method, Object[] args) {
		var type = method.getReturnType();
		if(Column.class.isAssignableFrom(type)) {
			var bind = requireNonNull(method.getAnnotation(Bind.class), 
					()-> "abstract method " + method + " must be annotated with @Bind");
			return buildColumn(method, bind, args);
		}
		if(Criteria.class.isAssignableFrom(type)) { //filter first (extends Column)
			throw new UnsupportedOperationException("not implemented");
		}
		if(JoinGroup.class.isAssignableFrom(type)) {
			throw new UnsupportedOperationException("not implemented");
		}
		if(Partition.class.isAssignableFrom(type)) {
			throw new UnsupportedOperationException("not implemented");
		}
		if(Order.class.isAssignableFrom(type)) {
			throw new UnsupportedOperationException("not implemented");
		}
		throw new IllegalStateException("unsupported method type " + method);
	}
	
	ViewColumn buildColumn(Method method, Bind bind, Object[] args) { //REF | REQ | SQL
		var typ = ofNullable(method.getAnnotation(Typed.class))
				.map(Typed::value)
				.orElseGet(()-> metadata.getColumnType(bind.value()));
		var tag = ofNullable(method.getAnnotation(Expose.class))
				.map(Expose::alias)
				.filter(not(String::isEmpty)) //optional
				.orElseGet(method::getName);
		return switch (bind.type()) {
		case REF -> new ViewColumn(bind.value(), view, typ, tag);
		default  -> throw new UnsupportedOperationException("not implemented");
		};
	}
	
	@Override
	int invokeHashCode(Object proxy, Object[] args) {
		return hash(view);
	}
	
	@Override
	String invokeToString(Object proxy, Object[] args) {
		return toSQL(view);
	}
	
	@Override
	public String toString() {
		return "DatasetProxy {"+view+"}";
	}

	static <T extends DatasetCatalogue> T createDataset(Class<T> type, Bind bind, String store, DataSource ds) {
		if(type.isInterface()) {
			var view = switch(bind.type()) {
			case REF-> new Table(bind.value(), store);
			//case REQ-> evalView(parseEntry(bind.value()), null)
			default -> throw new UnsupportedOperationException("not implemented " + bind.type());
			};
			var map = discoverExposedMethods(type, DatasetCatalogue.class, DatasetProxy::acceptBind);
			var cols = map.values().stream()
			.filter(m-> isAbstract(m.getModifiers()) && m.getAnnotation(Bind.class).type() == REF) //only binded object
			.map(m-> m.getAnnotation(Bind.class).value()).collect(toSet());
			var meta = datasetMetadata(store, view.getName(), cols, ds);
			return type.cast(newProxyInstance(DatasetProxy.class.getClassLoader(), new Class<?>[]{type}, 
					new DatasetProxy(view, map, Map.of(getMethod("getView", type), view), meta)));
		}
		throw new ResourceMappingException("view must be an interface : " + type);
	}
	
	static boolean acceptBind(Method method, Bind bind) {
		var t = bind.type();
		var c = method.getReturnType();
		if(t == REF) {
			return c == ViewColumn.class;
		}
		return Column.class.isAssignableFrom(c) || 
				Criteria.class.isAssignableFrom(c) || 
				c == Order.class || 
				c == Partition.class||
				c == JoinGroup.class;
	}
}
