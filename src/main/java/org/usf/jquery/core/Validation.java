package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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

	public static String requireLegalVariable(String s) {
		illegalArgumentIf(isNull(s) || !s.matches("[a-zA-Z]\\w*"), ()-> "illegal variable name: " + s);
		return s;
	}

	public static <T> T[] requireNoArgs(T[] args, Supplier<String> name) {
		illegalArgumentIf(nonNull(args) && args.length > 0, ()-> "'%s' takes no arguments".formatted(name.get()));
		return args;
	}

	public static <T> T[] requireNArgs(int n, T[] args, Supplier<String> name) {
		illegalArgumentIf(isNull(args) || args.length != n, ()-> "'%s' takes %d arguments".formatted(name.get(), n));
		return args;
	}

	public static <T> T[] requireAtLeastNArgs(int n, T[] args, Supplier<String> name) {
		illegalArgumentIf(isNull(args) || args.length < n, ()-> "'%s' takes at least %d arguments".formatted(name.get(), n));
		return args;
	}

	public static void illegalArgumentIf(boolean test, Supplier<String> supplier) {
		if(test) {
			throw new IllegalArgumentException(supplier.get());
		}
	}
}
