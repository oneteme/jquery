package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.DBObject.toSQL;
import static org.usf.jquery.web.proxy.Bind.BindType.REF;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupBindAnnotation;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupResourceAnnotation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;
import org.usf.jquery.web.ColumnMetadata;

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
			return obj instanceof DBColumn col ? mapColumn(method, col) : obj; //map column only
		}
		if(isAbstract(method.getModifiers())) {
			var bnd = method.getAnnotation(Bind.class); //required
			if(nonNull(bnd) && !bnd.value().isEmpty()) {
				return buildResource(method, bnd, args);
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
	
	DBColumn mapColumn(Method method, DBColumn col) {
		var rsr = method.getAnnotation(Entry.class); //exposed resource
		if(nonNull(rsr) && !rsr.tagname().isEmpty()) {
			col = col.as(rsr.tagname());
		}
		var typ = method.getAnnotation(Typed.class); //abstract & default method
		if(nonNull(typ)) {
			col = col.as(typ.value());
		}
		return col;
	}
	
	Object buildResource(Method method, Bind bind, Object[] args) {
		var type = method.getReturnType();
		if(DBColumn.class.isAssignableFrom(type)) {
			return buildColumn(method, bind, args);
		}
		if(DBFilter.class.isAssignableFrom(type)) { //filter first (extends Column)
			return buildFilter(method, bind, args);
		}
		if(ViewJoin[].class.isAssignableFrom(type)) {
			return buildJoin(method, bind, args);
		}
		if(Partition.class.isAssignableFrom(type)) {
			return buildPartition(method, bind, args);
		}
		if(DBOrder.class.isAssignableFrom(type)) {
			return buildOrder(method, bind, args);
		}
		throw new IllegalStateException("unsupported method type " + method);
	}
	
	ViewColumn buildColumn(Method method, Bind bind, Object[] args) { //REF | REQ | SQL
		var typ = ofNullable(method.getAnnotation(Typed.class))
				.map(Typed::value)
				.or(()-> ofNullable(metadata.get(bind.value())).map(ColumnMetadata::getType))
				.orElse(null);
		var tag = ofNullable(method.getAnnotation(Entry.class))
				.map(Entry::tagname)
				.filter(not(String::isEmpty))
				.orElseGet(method::getName);
		return switch (bind.type()) {
		case REF -> new ViewColumn(bind.value(), view, typ, tag);
		default  -> throw new UnsupportedOperationException("not implemented");
		};
	}
	
	DBFilter buildFilter(Method method, Bind bind, Object[] args) { 
		throw new UnsupportedOperationException("not implemented");
	}

	ViewJoin[] buildJoin(Method method, Bind bind, Object[] args) { 
		throw new UnsupportedOperationException("not implemented");
	}

	Partition buildPartition(Method method, Bind bind, Object[] args) {
		throw new UnsupportedOperationException("not implemented");
	}
	
	Partition buildOrder(Method method, Bind bind, Object[] args) { 
		throw new UnsupportedOperationException("not implemented");
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
