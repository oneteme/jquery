package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.Bind.BindType.REF;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.function.BiPredicate;

import org.usf.jquery.web.proxy.Bind.BindType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceScanner {

	public static void scanResources(Method[] methods, BiPredicate<BindType, Class<?>> matcher) {
		var resources = new HashMap<String, Method> ();
		stream(methods)
		.filter(m-> !isStatic(m.getModifiers()))
		.forEach(m-> resources.compute(validateResourceIdentifier(m, matcher), (k,v)->{ 
			if(isNull(v)) { 
				return m; 
			} 
			throw new IllegalArgumentException("duplicate resource name ["+k+"] : \n" + m + "\n" + v); 
		}));
	}
	
	public static String validateResourceIdentifier(Method m, BiPredicate<BindType, Class<?>> matcher) {
		if(isAbstract(m.getModifiers())) {
			var bnd = scanBinding(m, true);
			if(m.getParameterCount() > 0) { 
				throw new IllegalArgumentException("binded method cannot have parameters : " + m);
			}
			if(!matcher.test(bnd.type(), m.getReturnType())){
				throw new IllegalArgumentException("invalid @Bind.type=["+bnd.type()+"] for return type " + m.getReturnType() + " on " + m);
			}
		}
		else if(nonNull(m.getAnnotation(Bind.class))) { //bind default method 
			throw new IllegalArgumentException("cannot bind defaut medthod");
		}
		var id = m.getName();
		var exps = m.getAnnotation(Expose.class); 
		if(nonNull(exps) && !exps.identity().isEmpty()) {
			if(!exps.identity().matches("[a-zA-Z]\\w*")) { 
				throw new IllegalArgumentException("invalid @Expose.id=["+exps.identity()+"] on " + m); 
			}
			id = exps.identity();
		}
		return id;
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
