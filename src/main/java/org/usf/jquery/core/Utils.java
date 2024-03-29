package org.usf.jquery.core;

import java.util.Collection;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

	public static final int UNLIMITED = -1;
	
	public static boolean isEmpty(int[] a) {
		return a == null || a.length == 0;
	}
	
	public static <T> boolean isPresent(T[] a) {
		return !isEmpty(a);
	}
	
	public static <T> boolean isEmpty(T[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	public static boolean isEmpty(Map<?,?> c) {
		return c == null || c.isEmpty();
	}

	public static boolean isBlank(String str) {
		return str == null || str.isBlank();
	}
}
