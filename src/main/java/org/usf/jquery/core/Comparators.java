package org.usf.jquery.core;

import static org.usf.jquery.core.ArgTypeRef.firstArgJdbcType;
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
public interface Comparators {
	
	//basic comparator
	
	default TypedComparator eq() {
		return new TypedComparator(basicComparator("="), required(), required(firstArgJdbcType()));
	}

	default TypedComparator ne() {
		return new TypedComparator(basicComparator("<>"), required(), required(firstArgJdbcType()));
	}
	
	default TypedComparator lt() {
		return new TypedComparator(basicComparator("<"), required(), required(firstArgJdbcType()));
	}

	default TypedComparator le() {
		return new TypedComparator(basicComparator("<="), required(), required(firstArgJdbcType()));
	}

	default TypedComparator gt() {
		return new TypedComparator(basicComparator(">"), required(), required(firstArgJdbcType()));
	}

	default TypedComparator ge() {
		return new TypedComparator(basicComparator(">="), required(), required(firstArgJdbcType()));
	}
	
	default TypedComparator between() {
		return new TypedComparator(rangeComparator("BETWEEN"), required(), required(firstArgJdbcType()), required(firstArgJdbcType()));
	}
	
	//string comparator
	
	default TypedComparator startsLike() {
		return like(o-> o + "%");
	}

	default TypedComparator endsLike() {
		return like(o-> "%" + o);
	}

	default TypedComparator contentLike() {
		return like(o-> "%" + o + "%");
	}
	
	default TypedComparator startsNotLike() {
		return notLike(o-> o + "%");
	}

	default TypedComparator endsNotLike() {
		return notLike(o-> "%" + o);
	}

	default TypedComparator contentNotLike() {
		return notLike(o-> "%" + o + "%");
	}

	default TypedComparator like() {
		return like(null);
	}
	
	default TypedComparator iLike() {
		return iLike(null);
	}
	
	default TypedComparator notLike() {
		return notLike(null);
	}

	default TypedComparator notILike() {
		return notILike(null);
	}
	
	default TypedComparator like(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	default TypedComparator iLike(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	default TypedComparator notLike(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("NOT LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	default TypedComparator notILike(UnaryOperator<Object> wilcard) {
		return new TypedComparator(stringComparator("NOT ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}
	
	//null comparator
	
	default TypedComparator isNull() {
		return new TypedComparator(nullComparator("IS NULL"), required());
	}

	default TypedComparator notNull() { //isNotNUll
		return new TypedComparator(nullComparator("IS NOT NULL"), required());
	}
	
	//in comparator

	default TypedComparator in() {
		return new TypedComparator(inComparator("IN"), required(), required(firstArgJdbcType()), varargs(firstArgJdbcType()));
	}
	
	default TypedComparator notIn() {
		return new TypedComparator(inComparator("NOT IN"), required(), required(firstArgJdbcType()), varargs(firstArgJdbcType()));
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
	
	static InComparator inComparator(final String name) {
		return ()-> name;
	}

	static RangeComparator rangeComparator(final String name) {
		return ()-> name;
	}
}
