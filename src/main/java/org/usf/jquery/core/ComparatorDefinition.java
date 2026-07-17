package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.BOOLEAN;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class ComparatorDefinition extends Definition<Criteria> {

	private final Invocable comparator;
	
	public ComparatorDefinition(String name, Invocable comparator, Parameter... parameters) {
		super(name, BOOLEAN, parameters);
		this.comparator = comparator;
	}
	
	public SimplePredicate invokeAsExpression(Object... right) {
		return new SimplePredicate((b,args)-> invoke(args).build(b), right); //lazy arguments validation
	}
	
	@Override
	protected Criteria internalInvoke(JavaType type, Object... args) {
		if(type == BOOLEAN) {
			return new SimpleCriteria(args[0], new SimplePredicate(comparator, args, 1)); // no type
		}
		throw new IllegalStateException("comparator '%s' cannot be applied to type %s".formatted(this, type));
	}
}