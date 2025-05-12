package org.usf.jquery.core;

import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class TypedComparator implements Comparator {

	//do not @Delegate
	private final Comparator comparator;
	private final ParameterSet parameterSet;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this.comparator = comparator;
		this.parameterSet = ofParameters(parameters);
	}
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		comparator.build(query, parameterSet.assertArguments(args)); //assertArguments because expression
	}
	
	@Override
	public ComparisonSingleExpression expression(Object... right) {
		return Comparator.super.expression(right);  //cannot assertArguments because no left operand
	}
	
	@Override
	public ColumnSingleFilter filter(Object... args) {
		return comparator.filter(parameterSet.assertArguments(args));
	}
	@Override
	public String id() {
		return comparator.id();
	}
	
	@Override
	public boolean is(Class<? extends Comparator> type) {
		return comparator.is(type);
	}
	
	@Override
	public String toString() {
		return comparator.id() + parameterSet.toString();
	}
}
