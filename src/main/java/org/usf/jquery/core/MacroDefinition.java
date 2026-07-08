package org.usf.jquery.core;

import java.util.function.Function;

/**
 * 
 * @author u$f
 *
 */
public class MacroDefinition extends Definition<Column> {

	private final Function<Object[], Column> macro;
	
	public MacroDefinition(String name, TypeResolver typeFn, Function<Object[], Column> macro, Parameter... parameter) {
		super(name, typeFn, parameter);
		this.macro = macro;
	}

	@Override
	protected Column internalInvoke(JavaType type, Object... args) {
		var col = macro.apply(args);
		if(type.accept(col)) {
			return col;
		}
		throw new IllegalStateException("macro '%s' cannot be applied to type %s".formatted(getName(), type));
	}
}
