package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.ArgTypeRef.firstArgJdbcType;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;

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

	@Override
	default DBFilter args(Object... args) {
		return new ColumnSingleFilter(args[0], 
				expression(copyOfRange(args, 1, args.length))); // no type
	}

	//basic comparator

	default ComparisonExpression expression(Object... right) {
		return new ComparisonSingleExpression(this, right);
	}
	
	static TypedComparator eq() {
		return new TypedComparator(basicComparator("="), required(), required(firstArgJdbcType()));
	}

	static TypedComparator ne() {
		return new TypedComparator(basicComparator("<>"), required(), required(firstArgJdbcType()));
	}
	
	static TypedComparator lt() {
		return new TypedComparator(basicComparator("<"), required(), required(firstArgJdbcType()));
	}

	static TypedComparator le() {
		return new TypedComparator(basicComparator("<="), required(), required(firstArgJdbcType()));
	}

	static TypedComparator gt() {
		return new TypedComparator(basicComparator(">"), required(), required(firstArgJdbcType()));
	}

	static TypedComparator ge() {
		return new TypedComparator(basicComparator(">="), required(), required(firstArgJdbcType()));
	}
	
	//string comparator
	
	static TypedComparator startsLike() {
		return like(o-> o + "%");
	}

	static TypedComparator endsLike() {
		return like(o-> "%" + o);
	}

	static TypedComparator contentLike() {
		return like(o-> "%" + o + "%");
	}
	
	static TypedComparator startsNotLike() {
		return notLike(o-> o + "%");
	}

	static TypedComparator endsNotLike() {
		return notLike(o-> "%" + o);
	}

	static TypedComparator contentNotLike() {
		return notLike(o-> "%" + o + "%");
	}

	static TypedComparator like() {
		return like(null);
	}
	
	static TypedComparator iLike() {
		return iLike(null);
	}
	
	static TypedComparator notLike() {
		return notLike(null);
	}

	static TypedComparator notILike() {
		return notILike(null);
	}
	
	static TypedComparator like(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	static TypedComparator iLike(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	static TypedComparator notLike(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("NOT LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	static TypedComparator notILike(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("NOT ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}
	
	//null comparator
	
	static TypedComparator isNull() {
		return new TypedComparator(nullComparator("IS NULL"), required());
	}

	static TypedComparator notNull() { //isNotNUll
		return new TypedComparator(nullComparator("IS NOT NULL"), required());
	}
	
	//in comparator

	static TypedComparator in() {
		return new TypedComparator(inComparator("IN"), required(), varargs(firstArgJdbcType()));
	}
	
	static TypedComparator notIn() {
		return new TypedComparator(inComparator("NOT IN"), required(), varargs(firstArgJdbcType()));
	}
	
	static BasicComparator basicComparator(final String name) {
		return ()-> name;
	}
	
	static StringComparator stringComparator(final String name, UnaryOperator<Object> wilcard) {
		if(Objects.isNull(wilcard)) {
			return ()-> name;
		}
		return new StringComparator() {
			@Override
			public String id() {
				return name;
			}
			@Override
			public Object wildcardArg(Object o) {
				if(Objects.isNull(o) || o instanceof DBObject) {
					throw new UnsupportedOperationException("cannot wildcards parameter: " + o);
				}
				return wilcard.apply(o);
			}
		};
	}
	
	static NullComparator nullComparator(final String name) {
		return ()-> name;
	}
	
	static InCompartor inComparator(final String name) {
		return ()-> name;
	}
	
	static Optional<TypedComparator> lookupComparator(String op) {
		return DBProcessor.lookup(Comparator.class, TypedComparator.class, op);
	}
	
}
