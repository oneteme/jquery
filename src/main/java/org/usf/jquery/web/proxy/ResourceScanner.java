package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.web.proxy.Bind.BindType.REF;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiPredicate;

import org.usf.jquery.web.proxy.Bind.BindType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceScanner {

	public static Map<String, Method> scanExposedResources(Method[] methods, BiPredicate<BindType, Class<?>> allowBind) {
		return stream(methods)
		.filter(ResourceScanner::isExposedMedthod)
		.collect(toMap(m-> exposedResourceIdentifier(m, allowBind), identity()));
	}
	
	public static String exposedResourceIdentifier(Method m, BiPredicate<BindType, Class<?>> allowBind) {
		var id = m.getName();
		var exp = m.getAnnotation(Expose.class); 
		if(nonNull(exp) && !exp.identity().isEmpty()) {
			if(!exp.identity().matches("[a-zA-Z]\\w*")) { 
				throw new IllegalArgumentException("invalid @Expose.id=["+exp.identity()+"] on " + m); 
			}
			id = exp.identity();
		}
		if(isAbstract(m.getModifiers())) {
			var bnd = scanBinding(m, true);
			if(m.getParameterCount() > 0) { 
				throw new IllegalArgumentException("binded method cannot have parameters : " + m);
			}
			if(!allowBind.test(bnd.type(), m.getReturnType())){
				throw new IllegalArgumentException("invalid @Bind.type=["+bnd.type()+"] for return type " + m.getReturnType() + " on " + m);
			}
		}
		else if(nonNull(m.getAnnotation(Bind.class))) { //bind default method 
			throw new IllegalArgumentException("cannot bind defaut medthod");
		}
		return id;
	}
	
	static boolean isExposedMedthod(Method m) {
		if(!isStatic(m.getModifiers())) {
			var exp = m.getAnnotation(Expose.class);
			return isNull(exp) || exp.value();
		}
		return false;
	}
	
	public static Bind scanBinding(AnnotatedElement elem, boolean required){
		var bnd = elem.getAnnotation(Bind.class);
		if(nonNull(bnd)) {
			if(bnd.type() == REF && !bnd.value().matches("\\w+")) {
				throw new IllegalArgumentException("invalid @Bind.value=["+bnd.value()+"] on " + elem); 
			}	
			return bnd;
		}
		if(required) {
			throw new IllegalArgumentException("missing decorator @Bind on " + elem);
		}
		return null;
	}
}
