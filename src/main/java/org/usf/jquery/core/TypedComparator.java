package org.usf.jquery.core;

import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class TypedComparator implements Comparator {
	
	@Delegate
	private final Comparator comparator;
	private final ParameterSet parameterSet;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this.comparator = comparator;
		this.parameterSet = ofParameters(parameters);
	}

	@Override
	public ColumnSingleFilter args(Object... args) {
		args = parameterSet.match(args);
		return comparator.args(afterCheck(args));
	}
	
	public Comparator unwrap() {
		return comparator;
	}
	
	Object[] afterCheck(Object... args) {
		return args;
	}
}
