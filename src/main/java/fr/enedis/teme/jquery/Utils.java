package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

	public static boolean isEmpty(int[] a) {
		return a == null || a.length == 0;
	}
	
	public static boolean isEmpty(double[] a) {
		return a == null || a.length == 0;
	}
	
	public static <T> boolean isEmpty(T[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static boolean isBlank(String str) {
		return str == null || str.isBlank();
	}

	public static DBColumn[] concat(DBColumn[] c1, DBColumn[] c2) {
		return concat(c1, c2, DBColumn[]::new);
	}
	
	public static DBFilter[] concat(DBFilter[] c1, DBFilter[] c2) {
		return concat(c1, c2, DBFilter[]::new);
	}

	public static <T> T[] concat(T[] c1, T[] c2, IntFunction<T[]> fn) {
		//warn ArrayStoreException : cannot merge (enum, class)
		if(c1 != null && c2 != null) {
			return Stream.concat(Stream.of(c1), Stream.of(c2)).toArray(requireNonNull(fn));
		}
		return c1 == null ? c2 : c1;
	}

}
