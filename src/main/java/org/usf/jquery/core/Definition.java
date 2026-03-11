package org.usf.jquery.core;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Signature.compile;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Definition<T> {

	private final String name;
	private final JavaType returnType;
	private final TypeResolver returnTypeResover;
	private final Invocable<T> factory;
	private final Signature signature;

	public Definition(String name, JavaType type, Invocable<T> factory, Parameter... parameter) {
		this(name, type, null, factory, compile(parameter));
	}
	
	public Definition(String name, JavaType type, Invocable<T> factory, Signature signature) {
		this(name, type, null, factory, signature);
	}

	public Definition(String name, TypeResolver returnTypeResover, Invocable<T> factory, Parameter... parameter) {
		this(name, null, returnTypeResover, factory, compile(parameter));
	}
	
	public Definition(String name, TypeResolver returnTypeResover, Invocable<T> factory, Signature signature) {
		this(name, null, returnTypeResover, factory, signature);
	}
	
	public final T invoke(Object... args) {
		try {
			signature.match(args);
		} catch(SignatureMismatchException e) {
			throw new InvocationException(format("cannot invoke %s with arguments %s", this, Arrays.toString(args)), e);
		}
		var type = nonNull(returnType) ? returnType : returnTypeResover.apply(args);
		return factory.invoke(type, args);
	}
	
	public String toString() {
		return name + signature;
	}
}