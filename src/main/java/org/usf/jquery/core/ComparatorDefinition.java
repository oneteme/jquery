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

	private final Comparator comparator;
	
	public ComparatorDefinition(Comparator comparator, Parameter... parameters) {
		this(comparator.id(), comparator, parameters);
	}

	public ComparatorDefinition(String name, Comparator comparator, Parameter... parameters) {
		super(name, BOOLEAN, comparator, parameters);
		this.comparator = comparator;
	}
	
	//TODO cannot verify signature here, but it will be verified at runtime when invoked as criteria
	public SimplePredicate invokeAsExpression(Object... right) {
		return comparator.expression(right);  
	}
}