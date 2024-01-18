package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.ArgTypeRef.firstArgType;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JqueryType.FILTER;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * 
 * @author u$f
 *
 */
public interface Comparator extends DBProcessor<DBFilter> {
	
	String id();

	@Override
	default DBFilter args(Object... args) {
		return new ColumnSingleFilter((DBColumn)args[0], 
				this.expression(copyOfRange(args, 1, args.length))); // no type
	}

	//basic comparator

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
	
	//string comparator
	
	static TypedComparator startsLike() {
		return like().argsMapper(wildcardSecondArg(o-> o + "%"));
	}

	static TypedComparator endsLike() {
		return like().argsMapper(wildcardSecondArg(o-> "%" + o));
	}

	static TypedComparator contentLike() {
		return like().argsMapper(wildcardSecondArg(o-> "%" + o + "%"));
	}
	
	static TypedComparator startsNotLike() {
		return notLike().argsMapper(wildcardSecondArg(o-> o + "%"));
	}

	static TypedComparator endsNotLike() {
		return notLike().argsMapper(wildcardSecondArg(o-> "%" + o));
	}

	static TypedComparator contentNotLike() {
		return notLike().argsMapper(wildcardSecondArg(o-> "%" + o + "%"));
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
	
	//null comparator
	
	static TypedComparator isNull() {
		return new TypedComparator(nullComparator("IS NULL")); // takes no args
	}

	static TypedComparator notNull() {
		return new TypedComparator(nullComparator("IS NOT NULL")); // takes no args
	}
	
	//in comparator

	static TypedComparator in() {
		return new TypedComparator(inComparator("IN"), required(), varargs(firstArgType()));
	}
	
	static TypedComparator notIn() {
		return new TypedComparator(inComparator("NOT IN"), required(), varargs(firstArgType()));
	}
	
	//comparator chain
	
	static TypedComparator and() {
		return new TypedComparator(chain("AND"), required(FILTER), required(FILTER));
	}

	static TypedComparator or() {
		return new TypedComparator(chain("OR"), required(FILTER), required(FILTER));
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
	
	static ComparatorChain chain(final String name) {
		return ()-> name;
	}
	
	static Optional<TypedComparator> lookupComparator(String op) {
		return DBProcessor.lookup(Comparator.class, TypedComparator.class, op);
	}
	
	private static UnaryOperator<Object[]> wildcardSecondArg(UnaryOperator<Object> fn) {
		return args-> {
			args[1] = fn.apply(args[1]);
			return args;
		};
	}
	
	static IllegalArgumentException typeCannotBeNullException() {
		return new IllegalArgumentException("type cannot be null");
	}
}
