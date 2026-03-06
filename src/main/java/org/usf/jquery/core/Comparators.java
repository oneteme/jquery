package org.usf.jquery.core;

import static org.usf.jquery.core.TypeResolver.firstArgType;
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
	
	default ComparatorDefinition eq() {
		return new ComparatorDefinition(basicComparator("="), required(), required(firstArgType()));
	}

	default ComparatorDefinition ne() {
		return new ComparatorDefinition(basicComparator("<>"), required(), required(firstArgType()));
	}
	
	default ComparatorDefinition lt() {
		return new ComparatorDefinition(basicComparator("<"), required(), required(firstArgType()));
	}

	default ComparatorDefinition le() {
		return new ComparatorDefinition(basicComparator("<="), required(), required(firstArgType()));
	}

	default ComparatorDefinition gt() {
		return new ComparatorDefinition(basicComparator(">"), required(), required(firstArgType()));
	}

	default ComparatorDefinition ge() {
		return new ComparatorDefinition(basicComparator(">="), required(), required(firstArgType()));
	}
	
	default ComparatorDefinition between() {
		return new ComparatorDefinition(rangeComparator("BETWEEN"), required(), required(firstArgType()), required(firstArgType()));
	}
	
	//string comparator
	
	default ComparatorDefinition startsLike() {
		return like(o-> o + "%");
	}

	default ComparatorDefinition endsLike() {
		return like(o-> "%" + o);
	}

	default ComparatorDefinition contentLike() {
		return like(o-> "%" + o + "%");
	}
	
	default ComparatorDefinition startsNotLike() {
		return notLike(o-> o + "%");
	}

	default ComparatorDefinition endsNotLike() {
		return notLike(o-> "%" + o);
	}

	default ComparatorDefinition contentNotLike() {
		return notLike(o-> "%" + o + "%");
	}

	default ComparatorDefinition like() {
		return like(null);
	}
	
	default ComparatorDefinition iLike() {
		return iLike(null);
	}
	
	default ComparatorDefinition notLike() {
		return notLike(null);
	}

	default ComparatorDefinition notILike() {
		return notILike(null);
	}
	
	default ComparatorDefinition like(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	default ComparatorDefinition iLike(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	default ComparatorDefinition notLike(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("NOT LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	default ComparatorDefinition notILike(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("NOT ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}
	
	//null comparator
	
	default ComparatorDefinition isNull() {
		return new ComparatorDefinition(nullComparator("IS NULL"), required());
	}

	default ComparatorDefinition notNull() { //isNotNUll
		return new ComparatorDefinition(nullComparator("IS NOT NULL"), required());
	}
	
	//in comparator

	default ComparatorDefinition in() {
		return new ComparatorDefinition(inComparator("IN"), required(), required(firstArgType()), varargs(firstArgType()));
	}
	
	default ComparatorDefinition notIn() {
		return new ComparatorDefinition(inComparator("NOT IN"), required(), required(firstArgType()), varargs(firstArgType()));
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
