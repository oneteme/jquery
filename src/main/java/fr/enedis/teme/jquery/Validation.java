package fr.enedis.teme.jquery;

import java.util.Collection;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validation {
	
    public static String requireNonBlank(@NonNull String obj) {
		illegalArgumentIf(obj.isBlank(), "empty string");
		return obj;
	}

	public static <T> T[] requireNonEmpty(@NonNull T[] arr){
		illegalArgumentIf(arr.length == 0, "empty array");
		return arr;
	}
	
	public static <T> Collection<T> requireNonEmpty(@NonNull Collection<T> c){
		illegalArgumentIf(c.isEmpty(), "empty collection");
		return c;
	}

	public static void illegalArgumentIfNot(boolean test, String msg) {
		illegalArgumentIf(!test, msg);
	}
	
	public static void illegalArgumentIf(boolean test, String msg) {
		if(test) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void illegalArgumentIfNot(boolean test, @NonNull Supplier<String> supplier) {
		illegalArgumentIf(!test, supplier);
	}

	public static void illegalArgumentIf(boolean test, @NonNull Supplier<String> supplier) {
		if(test) {
			throw new IllegalArgumentException(supplier.get());
		}
	}

}
