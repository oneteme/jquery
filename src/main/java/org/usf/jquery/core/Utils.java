package org.usf.jquery.core;

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
	//move this
	static ThreadLocal<Database> context = new ThreadLocal<>(); // change it
	
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
	
	public static Database currentDatabase() {
		return context.get();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> String join(String delemiter, T... args) {
		return isNull(args) 
				? null 
				: Stream.of(args).map(Object::toString).collect(joining(delemiter));
	}

	@Deprecated
	public static void currentDatabase(Database db) {
		context.set(db);
	}
	
	public static <T> T[] arrayJoin(T[] arr, T o) {
		var res = copyOf(arr, arr.length+1);
		res[arr.length] = o;
		return res;
	}
}
