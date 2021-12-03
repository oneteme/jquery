package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.illegalArgumentIf;
import static java.util.Optional.ofNullable;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

	public static <T> boolean isEmpty(T[] a) {
		return a == null || a.length == 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}
	
	public static String nArgs(int nb){
		illegalArgumentIf(nb < 1, "n < 1");
        var s = "?";
        if(nb > 1){
            s += ",?".repeat(nb - 1);
        }
        return "(" + s + ")";
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
			return Stream.concat(Stream.of(c1), Stream.of(c2)).toArray(fn);
		}
		if(c1 != null) {
			return c1;
		}
		if(c2 != null) {
			return c2;
		}
		return null;
	}

	public static <T, R> R mapNullableOrNull(T o, Function<T, R> fn) {
		return ofNullable(o).map(fn).orElse(null);
	}

	public static <T> String mapNullableOrEmpty(T o, Function<T, String> fn) {
		return ofNullable(o).map(fn).orElse("");
	}
}
