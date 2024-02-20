package org.usf.jquery.core;

import static java.util.Objects.nonNull;
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
	private UnaryOperator<Object[]> argMapper;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this(comparator, ofParameters(parameters));
	}
	
	public TypedComparator(Comparator comparator, ParameterSet parameterSet) {
		this.comparator = comparator;
		this.parameterSet = parameterSet;
	}
	
	@Override
	public DBFilter args(Object... args) {
		args = parameterSet.args(args);
		if(nonNull(argMapper)) {
			args = argMapper.apply(args);
		}
		return comparator.args(args);
	}
	
	public Comparator unwrap() {
		return comparator;
	}
	
	public TypedComparator argsMapper(UnaryOperator<Object[]> argMapper) {
		this.argMapper = argMapper;
		return this;
	}
	
	@Override
	public String toString() {
		return comparator.id() + parameterSet.toString();
	}
}
