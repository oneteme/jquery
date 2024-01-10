package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Collection;

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
	static ThreadLocal<Database> context = new ThreadLocal<>();

	public static final int UNLIMITED = -1;
	
	public static <T> boolean isPresent(T[] a) {
		return nonNull(a) && a.length > 0;
	}
	
	public static <T> boolean isEmpty(T[] a) {
		return isNull(a) || a.length == 0;
	}
	
	public static boolean isEmpty(Collection<?> c) {
		return isNull(c) || c.isEmpty();
	}

	public static boolean isBlank(String s) {
		return isNull(s) || s.isBlank();
	}
	
	public static Database currentDatabase() {
		return context.get();
	}

	public static void currentDatabase(Database db) {
		context.set(db);
	}
}
