package org.usf.jquery.core;

import static java.util.Objects.nonNull;

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
	
	static BasicComparator equal() {
		return basicComparator("=");
	}

	static BasicComparator notEqual() {
		return basicComparator("<>");
	}
	
	static BasicComparator lessThan() {
		return basicComparator("<");
	}

	static BasicComparator lessOrEqual() {
		return basicComparator("<=");
	}

	static BasicComparator greaterThan() {
		return basicComparator(">");
	}

	static BasicComparator greaterOrEqual() {
		return basicComparator(">=");
	}
	
	static StringComparator like() {
		return stringComparator("LIKE");
	}
	
	static StringComparator iLike() {
		return stringComparator("ILIKE");
	}

	static StringComparator notLike() {
		return stringComparator("NOT LIKE");
	}

	static StringComparator notILike() {
		return stringComparator("NOT ILIKE");
	}

	static NullComparator isNull() {
		return nullComparator("IS NULL");
	}

	static NullComparator isNotNull() {
		return nullComparator("IS NOT NULL");
	}

	static InCompartor in() {
		return inComparator("IN");
	}
	
	static InCompartor notIn() {
		return inComparator("NOT IN");
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
