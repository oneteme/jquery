package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.BOOLEAN;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class ComparatorDefinition extends Definition<SimpleCriteria> {

	private final Comparator comparator;
	
	public ComparatorDefinition(Comparator comparator, Parameter... parameters) {
		super(comparator.id(), BOOLEAN, (type,args)-> comparator.filter(args), parameters);
		this.comparator = comparator;
	}
	
	public SimplePredicate applyAsExpression(Object... right) {
		return comparator.expression(right);  //cannot assertArguments because no left operand
	}
}