package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.usf.jquery.web.proxy.Bind.BindType.REF;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.usf.jquery.web.proxy.Bind.BindType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceUtils {

	public static Optional<Bind> lookupBindAnnotation(Class<?> elem) {
		var bind = elem.getAnnotation(Bind.class);
		return nonNull(bind) && !bind.value().isEmpty()
			? Optional.of(requiredBindAnnotation(elem, null,  t-> t==REF))
			: empty(); //default schema
	}
	
	public static Optional<Bind> lookupBindAnnotation(Method method, Function<Class<?>, Predicate<BindType>> fn) {
		if(isAbstract(method.getModifiers())) {
			return Optional.of(requiredBindAnnotation(method, method.getParameters(), fn.apply(method.getReturnType())));
		}
		else if(method.isAnnotationPresent(Bind.class)) { //bind default method
			throw new IllegalArgumentException("cannot bind default method " + method);
		}
		return empty();
	}
	
	public static Bind requiredBindAnnotation(AnnotatedElement elem, Parameter[] params, Predicate<BindType> typePred) {
		var bind = elem.getAnnotation(Bind.class);
		if(nonNull(bind)) {
			if(!typePred.test(bind.type())) {
				throw new IllegalArgumentException("illegal @Bind.type=["+bind.type()+"] on " + elem);
			}
			if(bind.type()==REF) {
				if(!bind.value().matches("[a-z]\\w*")) {
					throw new IllegalArgumentException("invalid @Bind.value=["+bind.value()+"] on " + elem); 
				}
				if(nonNull(params) && params.length > 0) {
					throw new IllegalArgumentException("@Bind.type=REF does not accept parameter : " + elem);
				}
			}//check parameters / tag bind
			else {
				throw new UnsupportedOperationException("not implemented");
			}
			return bind;
		}
		throw new IllegalArgumentException("@Bind is required for " + elem);
	}
	
	public static Optional<Resource> lookupResourceAnnotation(Method method) {
		var rsc = method.getAnnotation(Resource.class);
		if(nonNull(rsc)) {
			if(!rsc.value().isEmpty() && rsc.value().matches("\\w+")) {
				throw new IllegalArgumentException("invalid @Resource.value=["+rsc.value()+"] on " + method); 
			}
			if(!rsc.tagname().isEmpty() && rsc.tagname().matches("\\w+")) {
				throw new IllegalArgumentException("invalid @Resource.value=["+rsc.tagname()+"] on " + method); 
			}
			return Optional.of(rsc);
		}
		return empty();
	}
}
