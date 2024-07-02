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
	
	private final Comparator comparator;
	private final ParameterSet parameterSet;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this(comparator, ofParameters(parameters));
	}
	
	public TypedComparator(Comparator comparator, ParameterSet parameterSet) {
		this.comparator = comparator;
		this.parameterSet = parameterSet;
	}
	
	@Override
	public String id() {
		return comparator.id();
	}

	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		return comparator.sql(builder, parameterSet.assertArguments(args));
	}
	
	@Override
	public DBFilter args(Object... args) {
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
