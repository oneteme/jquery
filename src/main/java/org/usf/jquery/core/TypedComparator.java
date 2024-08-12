package org.usf.jquery.core;

import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class TypedComparator {
	
	private final Comparator comparator;
	private final ParameterSet parameterSet;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this.comparator = comparator;
		this.parameterSet = ofParameters(parameters);
	}
	
	public ComparisonExpression expression(Object... right) {
		return comparator.expression(parameterSet.assertArgumentsFrom(1, right)); //no left 
	}
	
	public DBFilter filter(Object... args) {
		return comparator.args(parameterSet.assertArguments(args));
	}

	public Comparator unwrap() {
		return comparator;
	}
	
	@Override
	public String toString() {
		return comparator.id() + parameterSet.toString();
	}
}
