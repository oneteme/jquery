package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Collection;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validation {
	
	public static final String VARIABLE_PATTERN = "[a-zA-Z]\\w*";
    
    public static String requireLegalVariable(String s) {
    	illegalArgumentIf(isNull(s) || !s.matches(VARIABLE_PATTERN), ()-> "illegal variable name : " + s);
		return s;
	}
	
    public static String requireNonBlank(String s) {
		illegalArgumentIf(isNull(s) || s.isBlank(), "empty string");
		return s;
	}

	public static <T> T[] requireNonEmpty(T[] arr){
		illegalArgumentIf(isNull(arr) || arr.length == 0, "empty array");
		return arr;
	}
	
	public static <T> Collection<T> requireNonEmpty(Collection<T> c){
		illegalArgumentIf(isNull(c) || c.isEmpty(), "empty collection");
		return c;
	}

	public static <T> T[] requireNoArgs(T[] args, Supplier<String> name) {
		illegalArgumentIf(nonNull(args) && args.length > 0, ()-> name.get() + " takes no parameters");
		return args;
	}

	public static <T> T[] requireNArgs(int n, T[] args, Supplier<String> name) {
		illegalArgumentIf(isNull(args) || args.length != n, ()-> name.get() + " takes " + n + " parameters");
		return args;
	}

	public static <T> T[] requireAtLeastNArgs(int n, T[] args, Supplier<String> name) {
		illegalArgumentIf(isNull(args) || args.length < n, ()-> name.get() + " takes at least " + n + " parameters");
		return args;
	}
	
	public static <T> T[] requireAtMostNArgs(int n, T[] args, Supplier<String> name) {
		illegalArgumentIf(nonNull(args) && args.length > n, ()-> name.get() + " takes at most" + n + " parameters");
		return args;
	}

	public static void illegalArgumentIf(boolean test, String msg) {
		if(test) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void illegalArgumentIf(boolean test, Supplier<String> supplier) {
		if(test) {
			throw new IllegalArgumentException(supplier.get());
		}
	}

}
