package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * 
 * @author u$f
 *
 */
public interface Comparator extends DBProcessor<ColumnSingleFilter> {
	
	String id();
	
	default boolean isVarargs() {
		return false;
	}
	
	@Override
	default ColumnSingleFilter args(Object... args) {
		if(Objects.nonNull(args) && args.length >= 1 && args.length <= 2) {
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
	
	static TypedComparator eq() {
		return new TypedComparator(basicComparator("="), required(), required());
	}

	static TypedComparator ne() {
		return new TypedComparator(basicComparator("<>"), required(), required());
	}
	
	static TypedComparator lt() {
		return new TypedComparator(basicComparator("<"), required(), required());
	}

	static TypedComparator le() {
		return new TypedComparator(basicComparator("<="), required(), required());
	}

	static TypedComparator gt() {
		return new TypedComparator(basicComparator(">"), required(), required());
	}

	static TypedComparator ge() {
		return new TypedComparator(basicComparator(">="), required(), required());
	}
	
	static TypedComparator startsLike() {
		return like().argsMapper(wildcardSecondArg(o-> o + "%"));
	}

	static TypedComparator endsLike() {
		return like().argsMapper(wildcardSecondArg(o-> "%" + o));
	}

	static TypedComparator contentLike() {
		return like().argsMapper(wildcardSecondArg(o-> "%" + o + "%"));
	}
	
	static TypedComparator like() {
		return new TypedComparator(stringComparator("LIKE"), required(VARCHAR), required(VARCHAR));
	}
	
	static TypedComparator iLike() {
		return new TypedComparator(stringComparator("ILIKE"), required(VARCHAR), required(VARCHAR));
	}

	static TypedComparator notLike() {
		return new TypedComparator(stringComparator("NOT LIKE"), required(VARCHAR), required(VARCHAR));
	}

	static TypedComparator notILike() {
		return new TypedComparator(stringComparator("NOT ILIKE"), required(VARCHAR), required(VARCHAR));
	}
	
	static TypedComparator isNull() {
		return new TypedComparator(nullComparator("IS NULL")); // takes no args
	}

	static TypedComparator nonNull() {
		return new TypedComparator(nullComparator("IS NOT NULL")); // takes no args
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
	
	private static UnaryOperator<Object[]> wildcardSecondArg(UnaryOperator<Object> fn) {
		return args->{
			args[1] = fn.apply(args[1]);
			return args;
		};
	}
	
	static IllegalArgumentException typeCannotBeNullException() {
		return new IllegalArgumentException("type cannot be null");
	}
}
