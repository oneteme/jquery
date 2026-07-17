package org.usf.jquery.core;

import java.util.function.Function;

/**
 * 
 * @author u$f
 *
 */
public final class ComposerDefinition<T> extends Definition<T> {

	private final Function<Object[], T> composer;

	public ComposerDefinition(String name, TypeResolver typeFn, Function<Object[], T> composer, Parameter... parameter) {
		super(name, typeFn, parameter);
		this.composer = composer;
	}
	
	public ComposerDefinition(String name, TypeResolver returnType, Function<Object[], T> composer, Signature signature) {
		super(name, returnType, signature);
		this.composer = composer;
	}

	@Override
	protected T internalInvoke(JavaType type, Object... args) {
		return composer.apply(args);
	}
}
