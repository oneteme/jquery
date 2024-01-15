package org.usf.jquery.core;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.copyOfRange;
import static java.util.Optional.empty;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.JqueryType.FILTER;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * 
 * @author u$f
 *
 */
public interface Comparator extends DBProcessor<DBFilter> {
	
	String id();
	
	default boolean isVarargs() {
		return false;
	}
	
	@Override
	default DBFilter args(Object... args) {
		return new ColumnSingleFilter((DBColumn)args[0], 
				this.expression(copyOfRange(args, 1, args.length))); // no type
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
	
	//pipe
	
	static TypedComparator and() {
		return new TypedComparator(pipe("AND"), required(FILTER), required(FILTER));
	}

	static TypedComparator or() {
		return new TypedComparator(pipe("OR"), required(FILTER), required(FILTER));
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
	
	static ComparatorPipe pipe(final String name) {
		return ()-> name;
	}
	
	static Optional<TypedComparator> lookupComparator(String op) {
		try {
			var m = Comparator.class.getMethod(op);
			if(isStatic(m.getModifiers()) && m.getReturnType() == TypedComparator.class  && m.getParameterCount() == 0) { // no private static
				return Optional.of((TypedComparator) m.invoke(null));
			}
		} catch (Exception e) {/* do not throw exception */}
		return empty();
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
