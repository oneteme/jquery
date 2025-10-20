package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.DBObject.toSQL;
import static org.usf.jquery.core.DBView.queryAsView;
import static org.usf.jquery.core.DBView.view;
import static org.usf.jquery.web.proxy.Bind.BindType.REF;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupBindAnnotation;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupResourceAnnotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;
import org.usf.jquery.web.ColumnMetadata;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ViewInvocationHandler implements InvocationHandler {

	private final DBView view;
	private final Map<String, ColumnMetadata> metadata = new LinkedHashMap<>();

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.isDefault()) {
			var obj = InvocationHandler.invokeDefault(proxy, method, args);
			return obj instanceof DBColumn col ? mapColumn(method, col) : obj; //map column only
		}
		if(isAbstract(method.getModifiers())) {
			if(DBFilter.class.isAssignableFrom(method.getReturnType())) { //filter first (extends Column)
				return buildFilter(method, args);
			}
			if(DBColumn.class.isAssignableFrom(method.getReturnType())) {
				return buildColumn(method, args); //type ??
			}
			if(ViewJoin[].class.isAssignableFrom(method.getReturnType())) {
				return buildJoin(method, args); //type ??
			}
			if(Partition.class.isAssignableFrom(method.getReturnType())) {
				return buildJoin(method, args); //type ??
			}
			throw new IllegalStateException("invoke " + method);
		}
		return switch (method.getName()) {
		case "equals"->  proxy == args[0];
		case "hashCode"-> hash(view);
		case "toString"-> toSQL(view);
		default -> throw new IllegalStateException("invoke " + method);
		};
	}
	
	DBColumn mapColumn(Method method, DBColumn col) {
		var rsrc = method.getAnnotation(Resource.class); //exposed resource
		if(nonNull(rsrc) && !rsrc.tagname().isEmpty()) {
			return col.as(rsrc.tagname());
		}
		var typed = method.getAnnotation(Typed.class); //abstract & default method
		if(nonNull(typed)) {
			return col.as(typed.value());
		}
		return col;
	}
	
	ViewColumn buildColumn(Method method, Object[] args) { //REF | REQ | SQL
		var bind = method.getAnnotation(Bind.class); //required
		var type = ofNullable(method.getAnnotation(Typed.class))
				.map(Typed::value)
				.or(()-> ofNullable(metadata.get(bind.value())).map(ColumnMetadata::getType))
				.orElse(null);
		var tag = ofNullable(method.getAnnotation(Resource.class))
				.map(Resource::value)
				.orElseGet(method::getName);
		return switch (bind.type()) {
		case REF -> new ViewColumn(bind.value(), view, type, tag);
		case SQL -> column(bind.value(), view, type, tag); //use args
		//TODO REQ
		default -> throw new UnsupportedOperationException(bind.type()+"");
		};
	}

	
	DBFilter buildFilter(Method method, Object[] args) { //REQ | SQL
		throw new UnsupportedOperationException();
	}

	ViewJoin[] buildJoin(Method method, Object[] args) { //REQ | SQL
		throw new UnsupportedOperationException();
	}

	Partition buildPartition(Method method, Object[] args) { //REQ | SQL
		throw new UnsupportedOperationException();
	}
	
//	if(isNull(bnd)) {
//		throw new IllegalArgumentException("missing decorator @ViewBind on " + m);
//	}
//	if(!bnd.name().matches("\\w+")) { //2 character at least
//		throw new IllegalArgumentException("invalid name " + bnd);
//	}
//	if(m.getParameterCount() > 0) {
//		throw new IllegalArgumentException();
//	}
	
	static <T extends ViewResource> T buildView(Class<T> clazz, Bind bind, String schema) {
		var view = switch(bind.type()) {
		case REF-> view(bind.value(), schema);
		case SQL-> queryAsView(bind.value());
		//TODO REQ
		default-> throw new UnsupportedOperationException();
		};
		return clazz.cast(newProxyInstance(ViewInvocationHandler.class.getClassLoader(), 
				new Class<?>[]{clazz}, new ViewInvocationHandler(view)));
	}

	static void validateViewResources(Class<?> clazz) {
		if(clazz.isInterface()) {
			stream(clazz.getMethods()).forEach(mth->{
				lookupBindAnnotation(mth, c->{
					if(DBColumn.class.isAssignableFrom(c)) {
						return t-> true;
					}
					if(c == DBFilter.class || c == ViewJoin[].class || c == Partition.class) {
						return t-> t != REF;
					}
					throw new IllegalArgumentException("illegal type " + c);
				});
				lookupResourceAnnotation(mth);
				var type = mth.getReturnType();
				if(DBColumn.class.isAssignableFrom(type)) {
					//validateColumnResource
				}
			});
		}
		else {
			throw new IllegalArgumentException(clazz + " is not a interface");
		}
	}
}
