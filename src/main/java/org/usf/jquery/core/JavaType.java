package org.usf.jquery.core;

import java.util.function.Predicate;

import lombok.NonNull;

/**
 * 
 * @author u$f
 * 
 */
public interface JavaType {
	
	boolean accept(Object o);
	
	Class<?> getType();
	
	static JavaType instance(Class<?> type) {
		return declare(type, type::isInstance);
	}
	
	static JavaType declare(@NonNull Class<?> type, @NonNull Predicate<Object> predicate) {
		return new JavaType() {
			@Override
			public Class<?> getType() {
				return type;
			}
			
			@Override
			public boolean accept(Object o) {
				return predicate.test(o);
			}
		};
	}
	
}