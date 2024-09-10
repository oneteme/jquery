package org.usf.jquery.core;

import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
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
	public String sql(QueryContext ctx, Object[] args) {
		try {
			return comparator.sql(ctx, parameterSet.assertArguments(args));
		} catch (BadArgumentException e) {
			throw badArgumentsException("comparator", comparator.id(), args, e);
		}
	}
	
	@Override // do not delegate this
	public ColumnSingleFilter filter(Object... args) {
		return Comparator.super.filter(args);
	}
	
	@Override
	public String toString() {
		return comparator.id() + parameterSet.toString();
	}
}
