package org.usf.jquery.core;

import java.util.Collection;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validation {
    
    public static String requireLegalVariable(String s) {
    	illegalArgumentIf(!requireNonBlank(s).matches("\\w+"), ()-> "illegal variable name : " + s);
		return s;
	}
	
    public static String requireNonBlank(@NonNull String s) {
		illegalArgumentIf(s.isBlank(), "empty string");
		return s;
	}

	public static <T> T[] requireNonEmpty(@NonNull T[] arr){
		illegalArgumentIf(arr.length == 0, "empty array");
		return arr;
	}
	
	public static <T> Collection<T> requireNonEmpty(@NonNull Collection<T> c){
		illegalArgumentIf(c.isEmpty(), "empty collection");
		return c;
	}

	public static void illegalArgumentIf(boolean test, String msg) {
		if(test) {
			throw new IllegalArgumentException(msg);
		}
	}

	@Deprecated
	public static void illegalArgumentIfNot(boolean test, @NonNull Supplier<String> supplier) {
		illegalArgumentIf(!test, supplier);
	}

	public static void illegalArgumentIf(boolean test, @NonNull Supplier<String> supplier) {
		if(test) {
			throw new IllegalArgumentException(supplier.get());
		}
	}

}
