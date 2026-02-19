package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
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
import java.util.function.Supplier;

import org.usf.jquery.web.proxy.Bind.BindType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceIntrospector {

	public static Map<String, Method> discoverExposedMethods(Class<?> type, BiPredicate<BindType, Class<?>> allowBind) {
		return stream(type.getDeclaredMethods()).<Method>mapMulti((m, c)-> {
			var mod = m.getModifiers();
			if(!isStatic(mod) && isPublic(mod)) {
				var exp = validateExpose(m);
				validateBind(m, allowBind);
				if(isNull(exp) || exp.value()) {
					c.accept(m);
				}
			}
		})
		.collect(toMap(ResourceIntrospector::resolveIdentifier, identity(), 
				(m1, m2) -> {
			        throw new ResourceMappingException("duplicate resource identifier: " + m1.getName() + " vs " + m2.getName());
			    }));
	}

	public static Bind validateBind(Method m, BiPredicate<BindType, Class<?>> allowBind){
		Bind bnd = null;
		if(isAbstract(m.getModifiers())) {
			bnd = scanBind(m);
			if(m.getParameterCount() > 0) { 
				throw new ResourceMappingException("binded method cannot have parameters : " + m);
			}
			if(!allowBind.test(bnd.type(), m.getReturnType())){
				throw new ResourceMappingException("invalid @Bind.type=["+bnd.type()+"] for return type " + m.getReturnType() + " on " + m);
			}
		}
		else if(nonNull(m.getAnnotation(Bind.class))) { //bind default method 
			throw new ResourceMappingException("default method cannot be binded : " + m);
		}
		return bnd;
	}

	public static Bind scanBind(AnnotatedElement elem){
		var bnd = elem.getAnnotation(Bind.class);
		if(nonNull(bnd)) {
			if(bnd.type() == REF) {
				verifyIdentifier(bnd.value(), () -> "invalid @Bind.value=["+bnd.value()+"] on " + elem);
			}	
			return bnd;
		}
		throw new ResourceMappingException("missing decorator @Bind on " + elem);
	}

	public static Expose validateExpose(Method m){
		var exp = m.getAnnotation(Expose.class); //optional annotation for exposed method
		if(nonNull(exp)) {
			if(!exp.identity().isEmpty()) {
				verifyIdentifier(exp.identity(), () -> "invalid @Expose.id=["+exp.identity()+"] on " + m);
			}
			if(!exp.alias().isEmpty()) {
				verifyIdentifier(exp.alias(), () -> "invalid @Expose.alias=["+exp.alias()+"] on " + m);
			}
		}
		return exp;
	}
	
	public static String resolveIdentifier(Method m) {
		var exp = m.getAnnotation(Expose.class);
		return nonNull(exp) && !exp.identity().isEmpty() 
				? exp.identity() 
				: m.getName();
	}
	
	static void verifyIdentifier(String id, Supplier<String> message) {
		if(isNull(id) || !id.matches("[a-zA-Z_]\\w*")) {
			throw new ResourceMappingException(message.get());
		}
	}
}
