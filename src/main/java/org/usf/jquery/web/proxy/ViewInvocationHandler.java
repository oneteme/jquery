package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.DBObject.toSQL;
import static org.usf.jquery.web.proxy.Bind.BindType.REF;
import static org.usf.jquery.web.proxy.ResourceScanner.scanMethods;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.web.ColumnMetadata;
import org.usf.jquery.web.spec.ViewResource;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
final class ViewInvocationHandler implements InvocationHandler {

	private final DBView view;
	private final Map<String, ColumnMetadata> metadata = new LinkedHashMap<>();

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.isDefault()) {
			var obj = InvocationHandler.invokeDefault(proxy, method, args);
			return obj instanceof DBColumn col ? decorate(method, col) : obj; //map column only
		}
		if(isAbstract(method.getModifiers())) {
			var bnd = method.getAnnotation(Bind.class); //required
			if(nonNull(bnd) && !bnd.value().isEmpty()) {
				return resolveBinding(method, bnd, args);
			}
			throw new IllegalArgumentException("missing decorator @Bind on " + method);
		}
		return switch (method.getName()) {
		case "equals"->  proxy == args[0];
		case "hashCode"-> hash(view);
		case "toString"-> toSQL(view);
		default -> throw new IllegalStateException("unexpected method invocation " + method);
		};
	}
	
	DBColumn decorate(Method method, DBColumn col) {
		var rsr = method.getAnnotation(Expose.class); //exposed resource
		if(nonNull(rsr) && !rsr.alias().isEmpty()) {
			col = col.as(rsr.alias());
		}
		var typ = method.getAnnotation(Typed.class); //abstract & default method
		if(nonNull(typ)) {
			col = col.as(typ.value());
		}
		return col;
	}
	
	Object resolveBinding(Method method, Bind bind, Object[] args) {
		var type = method.getReturnType();
		if(DBColumn.class.isAssignableFrom(type)) {
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
		if(DBOrder.class.isAssignableFrom(type)) {
			throw new UnsupportedOperationException("not implemented");
		}
		throw new IllegalStateException("unsupported method type " + method);
	}
	
	ViewColumn buildColumn(Method method, Bind bind, Object[] args) { //REF | REQ | SQL
		var typ = ofNullable(method.getAnnotation(Typed.class))
				.map(Typed::value)
				.or(()-> ofNullable(metadata.get(bind.value())).map(ColumnMetadata::getType))
				.orElse(null);
		var tag = ofNullable(method.getAnnotation(Expose.class))
				.map(Expose::alias)
				.filter(not(String::isEmpty))
				.orElseGet(method::getName);
		return switch (bind.type()) {
		case REF -> new ViewColumn(bind.value(), view, typ, tag);
		default  -> throw new UnsupportedOperationException("not implemented");
		};
	}

	static <T extends ViewResource> T bind(Class<T> clazz) {
		if(clazz.isInterface()) {
			scanMethods(clazz.getMethods(), (t,c)-> {
				if(t == REF) {
					return c == ViewColumn.class;
				}
				else {
					return DBColumn.class.isAssignableFrom(c) || 
							DBFilter.class.isAssignableFrom(c) || 
							c == DBOrder.class || 
							c == Partition.class||
							c == JoinsClause.class ;
				}
			});
		}
		throw new IllegalArgumentException("view must be an interface : " + clazz);
	}
}
