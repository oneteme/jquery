package org.usf.jquery.core;

import java.util.function.Function;

/**
 * 
 * @author u$f
 *
 */
public final class ComposerDefinition<T> extends Definition<T> {

	private final Function<Object[], T> macro;
	
	public ComposerDefinition(String name, TypeResolver returnType, Function<Object[], T> macro, Signature signature) {
		super(name, returnType, signature);
		this.macro = macro;
	}

	public ComposerDefinition(String name, TypeResolver typeFn, Function<Object[], T> macro, Parameter... parameter) {
		super(name, typeFn, parameter);
		this.macro = macro;
	}

	@Override
	protected T internalInvoke(JavaType type, Object... args) {
		var col = macro.apply(args);
//		if(type.getCorrespondingClass().isInstance(col)) {
//			return col;
//		}
//		throw new IllegalStateException(format("composer '%s' cannot be applied to type %s", getName(), type));
		return col;
	}
}
