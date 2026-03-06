package org.usf.jquery.core;

import static java.lang.String.format;
import static org.usf.jquery.core.Signature.compile;

import java.util.Arrays;
import java.util.function.BiFunction;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class Definition<T> {

	private final String name;
	private final TypeResolver returnType;
	private final BiFunction<JavaType, Object[], T> factory;
	private final Signature signature;

	public Definition(JavaType type, BiFunction<JavaType, Object[], T> builder, Parameter... parameter) {
		this(nameOf(type), o-> type, builder, parameter);
	}

	public Definition(String name, TypeResolver returnType, BiFunction<JavaType, Object[], T> factory, Parameter... parameter) {
		this.name = name;
		this.factory = factory;
		this.returnType = returnType;
		this.signature = compile(parameter);
	}
	
	public final T invoke(Object... args) {
		try {
			signature.match(args);
		} catch(SignatureMismatchException e) {
			throw new InvocationException(format("cannot invoke %s with arguments %s", this, Arrays.toString(args)), e);
		}
		return factory.apply(returnType.apply(args), args);
	}
	
	public String toString() {
		return name + signature;
	}
	
	static String nameOf(JavaType type) {
		return type instanceof Enum<?> e //for JDBC/JQuery types
				? e.name().toLowerCase()
				: type.toString();
	}	
}