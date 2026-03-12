package org.usf.jquery.core;

import static java.lang.String.format;
import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.JDBCType.BOOLEAN;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class ComparatorDefinition extends Definition<Criteria> {

	private final Comparator comparator;
	
	public ComparatorDefinition(String name, Comparator comparator, Parameter... parameters) {
		super(name, BOOLEAN, parameters);
		this.comparator = comparator;
	}
	
	//TODO cannot verify signature here, but it will be verified at runtime when invoked as criteria
	public SimplePredicate invokeAsExpression(Object... right) {
		return new SimplePredicate(comparator, right, null);
	}
	
	@Override
	protected Criteria internalInvoke(JavaType type, Object... args) {
		if(type == BOOLEAN) {
			return new SimpleCriteria(args[0], 
					new SimplePredicate(comparator, copyOfRange(args, 1, args.length), null)); // no type
		}
		throw new IllegalStateException(format("comparator '%s' cannot be applied to type %s", this, type));
	}
}