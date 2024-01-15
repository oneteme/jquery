package org.usf.jquery.core;

import static java.util.function.UnaryOperator.identity;
import static org.usf.jquery.core.ParameterSet.ofParameters;

import java.util.function.UnaryOperator;

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
	private UnaryOperator<Object[]> argMapper = identity();
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this.comparator = comparator;
		this.parameterSet = ofParameters(parameters);
	}

	@Override
	public DBFilter args(Object... args) {
		args = parameterSet.match(args);
		return comparator.args(argMapper.apply(args));
	}
	
	public Comparator unwrap() {
		return comparator;
	}
	
	TypedComparator argsMapper(UnaryOperator<Object[]> argMapper) {
		this.argMapper = argMapper;
		return this;
	}
}
