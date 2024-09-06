package org.usf.jquery.core;

import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class TypedComparator implements Comparator {
	
	private final Comparator comparator; // do not delegate
	private final ParameterSet parameterSet;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this.comparator = comparator;
		this.parameterSet = ofParameters(parameters);
	}
	
	@Override
	public String id() {
		return comparator.id();
	}
	
	@Override
	public String sql(QueryVariables builder, Object[] args) {
		try {
			return comparator.sql(builder, parameterSet.assertArguments(args));
		} catch (BadArgumentException e) {
			throw badArgumentsException("comparator", comparator.id(), args, e);
		}
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
