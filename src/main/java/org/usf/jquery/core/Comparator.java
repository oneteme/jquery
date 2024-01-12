package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Comparator extends DBProcessor<ColumnSingleFilter> {
	
	@Override
	default ColumnSingleFilter args(Object... args) {
		if(nonNull(args) && args.length == 2) {
			if(args[0] instanceof DBColumn) {
				return new ColumnSingleFilter((DBColumn)args[0], 
						this.expression(args.length > 1 ? args[1] : null)); // no type
			}
			else {
				throw new IllegalArgumentException(); //TODO msg
			}
		}
		throw new IllegalArgumentException(); //TODO msg
	}

	default ComparisonExpression expression(Object right) {
		return new ComparisonSingleExpression(this, right);
	}
	
	static TypedComparator equal() { //eq
		return new TypedComparator(basicComparator("="), required(), required());
	}

	static TypedComparator notEqual() { //ne
		return new TypedComparator(basicComparator("<>"), required(), required());
	}
	
	static TypedComparator lessThan() { //lt
		return new TypedComparator(basicComparator("<"), required(), required());
	}

	static TypedComparator lessOrEqual() { //le
		return new TypedComparator(basicComparator("<="), required(), required());
	}

	static TypedComparator greaterThan() { //gt
		return new TypedComparator(basicComparator(">"), required(), required());
	}

	static TypedComparator greaterOrEqual() { //ge
		return new TypedComparator(basicComparator(">="), required(), required());
	}
	
	static TypedComparator like() {
		return new TypedComparator(stringComparator("LIKE"), required(), required());
	}
	
	static TypedComparator iLike() {
		return new TypedComparator(stringComparator("ILIKE"), required(), required());
	}

	static TypedComparator notLike() {
		return new TypedComparator(stringComparator("NOT LIKE"), required(), required());
	}

	static TypedComparator notILike() {
		return new TypedComparator(stringComparator("NOT ILIKE"), required(), required());
	}

	static TypedComparator isNull() {
		return new TypedComparator(nullComparator("IS NULL"));
	}

	static TypedComparator isNotNull() {
		return new TypedComparator(nullComparator("IS NOT NULL"));
	}

	static TypedComparator in() {
		return new TypedComparator(inComparator("IN"), required(), varargs());
	}
	
	static TypedComparator notIn() {
		return new TypedComparator(inComparator("NOT IN"), required(), varargs());
	}
	
	static BasicComparator basicComparator(final String name) {
		return ()-> name;
	}
	
	static StringComparator stringComparator(final String name) {
		return ()-> name;
	}
	
	static NullComparator nullComparator(final String name) {
		return ()-> name;
	}
	
	static InCompartor inComparator(final String name) {
		return ()-> name;
	}
}
