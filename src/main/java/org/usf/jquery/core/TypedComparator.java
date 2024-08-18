package org.usf.jquery.core;

import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.Utils.join;

import java.util.Arrays;

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
		try {
			return comparator.expression(parameterSet.assertArgumentsFrom(1, right)); //no left 
		} catch (BadArgumentException e) {
			throw badArgumentsException(toString(), comparator.id() + join(SCOMA, "(", ")", right), e);
		}
	}
	
	public DBFilter filter(Object... args) {
		try {
			return comparator.args(parameterSet.assertArguments(args));
		} catch (BadArgumentException e) {
			throw badArgumentsException(toString(), comparator.id() + join(SCOMA, "(", ")", args), e);
		}
	}

	public Comparator unwrap() {
		return comparator;
	}
	
	@Override
	public String toString() {
		return comparator.id() + parameterSet.toString();
	}
}
