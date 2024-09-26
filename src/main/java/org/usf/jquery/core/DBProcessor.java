package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * 
 * @author u$f
 *
 */
public interface DBProcessor extends DBObject {

	static <T,U> Optional<U> lookup(Class<T> clazz, Class<U> type, String name, Predicate<? super U> pre) {
		try {
			var m = clazz.getMethod(name); //no parameter
			if(m.getReturnType() == type && m.getParameterCount() == 0 && isStatic(m.getModifiers()) && !isPrivate(m.getModifiers())) {
				var o = type.cast(m.invoke(null));
				if(isNull(pre) || pre.test(o)) {
					return Optional.of(o);
				}
			}
		} catch (Exception e) {/* do not throw exception */}
		return empty();
	}
}
