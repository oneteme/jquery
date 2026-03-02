package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.usf.jquery.core.DBObject.toSQL;
import static org.usf.jquery.web.proxy.Bind.BindType.REF;
import static org.usf.jquery.web.proxy.DatabaseIntrospector.fetchMetadata;
import static org.usf.jquery.web.proxy.ResourceIntrospector.discoverExposedMethods;
import static org.usf.jquery.web.proxy.ViewMetadata.noMetadata;

import java.lang.reflect.Method;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.TableView;
import org.usf.jquery.core.ViewColumn;

/**
 * 
 * @author u$f
 *
 */
final class DatasetProxy extends ResourceProxy {

	private final DBView view;
	private final ViewMetadata metadata;
	
	DatasetProxy(DBView view, Map<String, Method> resourceMap, ViewMetadata metadata) {
		super(resourceMap);
		this.view = view;
		this.metadata = metadata;
	}

	@Override
	Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
		var res = super.invokeDefaultMethod(proxy, method, args);
		if(res instanceof DBColumn col) {
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
		if(method.getReturnType() == DBView.class && method.getParameterCount() == 0 && method.getName().equals("getView")) {
			return view;
		}
		var type = method.getReturnType();
		if(DBColumn.class.isAssignableFrom(type)) {
			var bind = requireNonNull(method.getAnnotation(Bind.class), 
					()-> "abstract method " + method + " must be annotated with @Bind");
			return buildColumn(method, bind, args);
		}
		if(DBFilter.class.isAssignableFrom(type)) { //filter first (extends Column)
			throw new UnsupportedOperationException("not implemented");
		}
		if(JoinsClause.class.isAssignableFrom(type)) {
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

	static <T extends DatasetResource> T createDataset(Class<T> type, Bind bind, String schema, DataSource ds) {
		if(type.isInterface()) {
			var view = switch(bind.type()) {
			case REF-> new TableView(bind.value(), schema, null);
			//case REQ-> evalView(parseEntry(bind.value()), null)
			default -> throw new UnsupportedOperationException("not implemented " + bind.type());
			};
			var map = discoverExposedMethods(type, (t,c)-> {
				if(t == REF) {
					return c == ViewColumn.class;
				}
				else {
					return DBColumn.class.isAssignableFrom(c) || 
							DBFilter.class.isAssignableFrom(c) || 
							c == Order.class || 
							c == Partition.class||
							c == JoinsClause.class;
				}
			});
			var cols = stream(type.getDeclaredMethods())
			.filter(m-> isAbstract(m.getModifiers()) && m.getAnnotation(Bind.class).type() == REF) //must have bind annotation
			.map(m-> m.getAnnotation(Bind.class).value())
			.collect(toSet());
			
			var meta = nonNull(ds) 
					? fetchMetadata(schema, view.getName(), cols, ds) 
					: noMetadata(view.getName());
			
			return type.cast(newProxyInstance(DatasetProxy.class.getClassLoader(), new Class<?>[]{type}, 
					new DatasetProxy(view, map, meta)));
		}
		throw new ResourceMappingException("view must be an interface : " + type);
	}
}
