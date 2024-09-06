package org.usf.jquery.core;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

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
	
	public static <T> boolean isEmpty(T[] a) {
		return isNull(a) || a.length == 0;
	}
	
	public static boolean isEmpty(Collection<?> c) {
		return isNull(c) || c.isEmpty();
	}
	
	public static boolean isEmpty(Map<?,?> c) {
		return isNull(c) || c.isEmpty();
	}

	public static boolean isBlank(String s) {
		return isNull(s) || s.isBlank();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> String joinArray(String delemiter, T... args) {
		return joinAndDelemitArray(delemiter, "", "", args);
	}

	@SuppressWarnings("unchecked")
	public static <T> String joinAndDelemitArray(String delemiter, String before, String after, T... args) {
		return isNull(args) 
				? before + after 
				: joinAndDelemit(delemiter, before, after, Stream.of(args));
	}
	
	public static <T> String join(String delemiter, Stream<T> args) {
		return joinAndDelemit(delemiter, "", "", args);
	}
	
	public static <T> String joinAndDelemit(String delemiter, String before, String after, Stream<T> args) {
		return args.map(Object::toString).collect(joining(delemiter, before, after));
	}
	
	public static <T> T[] appendLast(T[] arr, T o) {
		var res = copyOf(arr, arr.length+1);
		res[arr.length] = o;
		return res;
	}

	public static Object[] appendFirst(Object[] arr, Object o) {
		var res = new Object[arr.length+1];
		arraycopy(arr, 0, res, 1, arr.length);
		res[0] = o;
		return res;
	}
}
