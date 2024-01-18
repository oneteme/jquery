package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Optional.empty;

import java.util.Optional;

/**
 * 
 * @author u$f
 *
 */
public interface DBProcessor<T> extends DBObject {
	
	T args(Object... args);
	
	static <T,U extends T> Optional<U> lookup(Class<T> clazz, Class<U> ext, String op) {
		try {
			var m = clazz.getMethod(op);
			if(m.getReturnType() == ext && isStatic(m.getModifiers()) && m.getParameterCount() == 0) { // no private static
				return Optional.of(ext.cast(m.invoke(null)));
			}
		} catch (Exception e) {/* do not throw exception */}
		return empty();
	}
}
