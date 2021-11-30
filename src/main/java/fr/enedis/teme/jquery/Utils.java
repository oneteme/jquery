package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import java.util.function.IntFunction;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

	public static <T> T[] requireNonEmpty(T[] c, String name){
		
		if(isEmpty(requireNonNull(c))) {
			throw new IllegalArgumentException(name + " canot be empty");
		}
		return c;
	}

    public static <T> boolean isEmpty(T[] a) {
    	return a == null || a.length == 0;
    }

	public static String nArgs(int nb){
        if(nb < 1){
            throw new IllegalArgumentException("n < 1");
        }
        var s = "?";
        if(nb > 1){
            s += ",?".repeat(nb - 1);
        }
        return "(" + s + ")";
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

}
