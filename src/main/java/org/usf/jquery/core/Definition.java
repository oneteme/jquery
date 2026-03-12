package org.usf.jquery.core;

import static java.lang.String.format;
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
@RequiredArgsConstructor
public abstract class Definition<R> {

	private final String name;
	private final TypeResolver returnType;
	private final Signature signature;

	Definition(String name, TypeResolver returnType, Parameter... parameter) {
		this(name, returnType, compile(parameter));
	}
	
	public final R invoke(Object... args) {
		try {
			signature.match(args);
		} catch(SignatureMismatchException e) {
			throw new InvocationException(format("cannot invoke %s with arguments %s", this, Arrays.toString(args)), e);
		}
		var type = returnType.apply(args);
		return internalInvoke(type, args);
	}
	
	protected abstract R internalInvoke(JavaType type, Object... args);
	
	public String toString() {
		return name + signature;
	}
}