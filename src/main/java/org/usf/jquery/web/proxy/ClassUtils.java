package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.lang.reflect.Method;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ClassUtils {

	public static Method getMethod(String name, Class<?> clazz, Class<?>... argsClazz)  {
		try {
			return clazz.getMethod(name, argsClazz);
		} catch (Exception e) {
			throw new NoSuchMethodError("no such method: " + clazz.getName() + "." + name 
					+ "("+ stream(argsClazz).map(Class::getSimpleName).collect(joining(",")) + ")");
		}
	}

	public static Method lookupAccessibleMethod(String name, Class<?> clazz, Class<?>... argsClazz) {
		try {
			var m = clazz.getMethod(name, argsClazz);
			var mod = m.getModifiers();
			if(isPublic(mod) && !isStatic(mod)) {
				return m;
			}
		} catch (Exception e) {
			log.trace("no such method: {}.{}()", clazz.getName(), name);
		}
		return null;
	}

}
