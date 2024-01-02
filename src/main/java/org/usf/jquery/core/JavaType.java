package org.usf.jquery.core;

import static java.util.Objects.isNull;

import java.util.function.Predicate;

import lombok.NonNull;

/**
 * 
 * @author u$f
 * 
 */
public interface JavaType {
	
	String name();
	
	Class<?> type();
	
	boolean accept(Object o);
	
	static JavaType instance(Class<?> type) {
		return declare(type.getSimpleName(), type, o-> isNull(o) || type.isInstance(o));
	}
	
	static JavaType declare(@NonNull String name, @NonNull Class<?> type, @NonNull Predicate<Object> predicate) {
		return new JavaType() {
			
			@Override
			public String name() {
				return name;
			}
			
			@Override
			public Class<?> type() {
				return type;
			}
			
			@Override
			public boolean accept(Object o) {
				return predicate.test(o);
			}
		};
	}
}