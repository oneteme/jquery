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

	//do not @Delegate
	private final Comparator comparator;
	private final ParameterSet parameterSet;
	
	public TypedComparator(Comparator comparator, Parameter... parameters) {
		this.comparator = comparator;
		this.parameterSet = ofParameters(parameters);
	}
	
	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		try {
			comparator.sql(sb, ctx, parameterSet.assertArguments(args));
		} catch (BadArgumentException e) {
			throw badArgumentsException("comparator", comparator.id(), args, e);
		}
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
