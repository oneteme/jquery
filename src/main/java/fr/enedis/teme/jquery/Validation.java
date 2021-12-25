package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validation {
	
    public static String requireNonBlank(String obj) {
		illegalArgumentIf(requireNonNull(obj).isBlank(), "empty string");
		return obj;
	}

	public static <T> T[] requireNonEmpty(T[] arr){
		illegalArgumentIf(requireNonNull(arr).length == 0, "empty array");
		return arr;
	}
	
	public static <T> Collection<T> requireNonEmpty(Collection<T> c){
		illegalArgumentIf(requireNonNull(c).isEmpty(), "empty collection");
		return c;
	}
	
	public static void illegalArgumentIf(boolean test, String msg) {
		if(test) {
			throw new IllegalArgumentException(requireNonNull(msg));
		}
	}

	public static void illegalArgumentIf(boolean test, Supplier<String> supplier) {
		if(test) {
			throw new IllegalArgumentException(requireNonNull(supplier).get());
		}
	}

}
