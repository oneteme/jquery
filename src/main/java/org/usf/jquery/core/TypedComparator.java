package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.Utils.isEmpty;

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
	private final ParameterSet[] overloads;
	private UnaryOperator<Object[]> argMapper;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this(comparator, ofParameters(parameters));
	}
	
	public TypedComparator(Comparator comparator, ParameterSet parameterSet, ParameterSet... overloads) {
		this.comparator = comparator;
		this.parameterSet = parameterSet;
		this.overloads = overloads;
	}
	@Override
	public DBFilter args(Object... args) {
		try {
			return internalArgs(parameterSet, args);
		} catch (RuntimeException e) {
			if(!isEmpty(overloads)) {
				for(var ps : overloads) {
					try {
						return internalArgs(ps, args);
					} catch (RuntimeException e1) { /* do not throw exception */ }
				}
			}
			throw e; //wrap exception
		}
	}

	private DBFilter internalArgs(ParameterSet ps, Object... args) {
		args = ps.args(args);
		return comparator.args(isNull(argMapper) ? args : argMapper.apply(args));
	}
	
	public Comparator unwrap() {
		return comparator;
	}
	
	public TypedComparator argsMapper(UnaryOperator<Object[]> argMapper) {
		this.argMapper = argMapper;
		return this;
	}
}
