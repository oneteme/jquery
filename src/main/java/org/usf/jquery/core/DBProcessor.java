package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isPrivate;
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
	
	static <T,U> Optional<U> lookup(Class<T> clazz, Class<U> ext, String op) {
		try {
			var m = clazz.getMethod(op); //no parameter
			if(m.getReturnType() == ext && isStatic(m.getModifiers()) && !isPrivate(m.getModifiers())) {
				return Optional.of(ext.cast(m.invoke(null)));
			}
		} catch (Exception e) {/* do not throw exception */}
		return empty();
	}
}
