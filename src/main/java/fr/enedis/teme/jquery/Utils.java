package fr.enedis.teme.jquery;

import java.util.function.IntFunction;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {
	
	public static <T> boolean isEmpty(T[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isBlank(String str) {
		return str == null || str.isBlank();
	}

	public static TaggableColumn[] concat(TaggableColumn[] c1, TaggableColumn[] c2) {
		return concat(c1, c2, TaggableColumn[]::new);
	}
	
	public static DBFilter[] concat(DBFilter[] c1, DBFilter[] c2) {
		return concat(c1, c2, DBFilter[]::new);
	}

	public static QueryDataJoiner[] concat(QueryDataJoiner[] c1, QueryDataJoiner... c2) {
		return concat(c1, c2, QueryDataJoiner[]::new);
	}

	public static <T> T[] concat(T[] c1, T[] c2, @NonNull IntFunction<T[]> fn) {
		//warn ArrayStoreException : cannot merge (enum, class)
		if(c1 != null && c2 != null) {
			return Stream.concat(Stream.of(c1), Stream.of(c2)).toArray(fn);
		}
		return c1 == null ? c2 : c1;
	}
	
}
