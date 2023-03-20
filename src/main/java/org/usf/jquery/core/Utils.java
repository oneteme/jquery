package org.usf.jquery.core;

import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {
	
	public static <T> boolean isEmpty(T[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}
	
	public static <T> boolean isPresent(T[] a) {
		return !isEmpty(a);
	}

	public static boolean isBlank(String str) {
		return str == null || str.isBlank();
	}

	public static <T> boolean hasSize(T[] a, int size) {
		return a != null && a.length == size;
	}
	

	public static <T> boolean hasSize(T[] a, IntPredicate size) {
		return a != null && size.test(a.length);
	}
	
	public static <T, R> Map<R, T> toMap(Collection<T> c, Function<T, R> fn){
		
		return c.stream().collect(Collectors.toMap(fn, identity()));
	}
	
}
