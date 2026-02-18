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
import java.util.function.Supplier;

import org.usf.jquery.web.proxy.Bind.BindType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceScanner {

	public static Map<String, Method> scanExposedResources(Method[] methods, BiPredicate<BindType, Class<?>> allowBind) {
		return stream(methods).<Method>mapMulti((m, c)-> {
			if(!isStatic(m.getModifiers())) {
				var exp = validateExpose(m);
				if(isNull(exp) || exp.value()) {
					validateBind(m, allowBind);
					validateParameterized(m);
					c.accept(m);
				}
			}
		})
		.collect(toMap(ResourceScanner::getMethodIdentifier, identity(), 
				(m1, m2) -> {
			        throw new JQueryConfigurationException("duplicate resource identifier: " + m1.getName() + " vs " + m2.getName());
			    }));
	}

	public static Bind validateBind(Method m, BiPredicate<BindType, Class<?>> allowBind){
		Bind bnd = null;
		if(isAbstract(m.getModifiers())) {
			bnd = scanBinding(m, true);
			if(m.getParameterCount() > 0) { 
				throw new JQueryConfigurationException("binded method cannot have parameters : " + m);
			}
			if(!allowBind.test(bnd.type(), m.getReturnType())){
				throw new JQueryConfigurationException("invalid @Bind.type=["+bnd.type()+"] for return type " + m.getReturnType() + " on " + m);
			}
		}
		else if(nonNull(m.getAnnotation(Bind.class))) { //bind default method 
			throw new JQueryConfigurationException("default method cannot be binded : " + m);
		}
		return bnd;
	}

	public static Bind scanBinding(AnnotatedElement elem, boolean required){
		var bnd = elem.getAnnotation(Bind.class);
		if(nonNull(bnd)) {
			if(bnd.type() == REF) {
				verifyIdentifier(bnd.value(), () -> "invalid @Bind.value=["+bnd.value()+"] on " + elem);
			}	
			return bnd;
		}
		if(required) {
			throw new JQueryConfigurationException("missing decorator @Bind on " + elem);
		}
		return null;
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

	public static Parameterized validateParameterized(Method m){
		var prm = m.getAnnotation(Parameterized.class); //optional annotation for parameterized method
		if(nonNull(prm)) {
			if(m.getParameterCount() == 0) { 
				throw new JQueryConfigurationException("parameterized method must have parameters : " + m);
			}
			var type = prm.parser();
			if(type.isInterface() 
					|| isAbstract(type.getModifiers())
					|| stream(type.getConstructors()).noneMatch(c-> c.getParameterCount() == 0)) {
				throw new JQueryConfigurationException("invalid @Parameterized.parser=["+type+"] on " + m); 
			}
		}
		return prm;
	}
	
	public static String getMethodIdentifier(Method m) {
		var exp = m.getAnnotation(Expose.class);
		return nonNull(exp) && !exp.identity().isEmpty() ? exp.identity() : m.getName();
	}
	
	static void verifyIdentifier(String id, Supplier<String> message) {
		if(isNull(id) || !id.matches("[a-zA-Z_]\\w*")) {
			throw new JQueryConfigurationException(message.get());
		}
	}
}
