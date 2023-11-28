package org.usf.jquery.core;

import static java.util.Objects.isNull;

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
		return isNull(a) || a.length == 0;
	}
	
	public static boolean isEmpty(Collection<?> c) {
		return isNull(c) || c.isEmpty();
	}

	public static boolean isEmpty(Map<?,?> map) {
		return isNull(map) || map.isEmpty();
	}

	public static boolean isBlank(String s) {
		return isNull(s) || s.isBlank();
	}
}
