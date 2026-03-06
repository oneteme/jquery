package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.TypeResolver.firstArgType;
import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * 
 * @author u$f
 *
 */
public interface Comparator extends DBProcessor {
	
	String id();
	
	@Override
	default int compose(QueryComposer composer, Consumer<Column> groupKeys) {
		throw new UnsupportedOperationException("compose comparator");
	}

	default SimpleCriteria filter(Object... args) {
		return new SimpleCriteria(args[0], 
				expression(copyOfRange(args, 1, args.length))); // no type
	}

	default SimplePredicate expression(Object... right) {
		return new SimplePredicate(this, right, null);
	}
	
	@Deprecated
	default SimplePredicate expression(Adjuster<Object[]> adj, Object... initalValue) {
		return new SimplePredicate(this, initalValue, adj);
	}
	
	default boolean is(Class<? extends Comparator> type) { 
		return type.isInstance(this);
	}

	//basic comparator
	
	static ComparatorDefinition eq() {
		return new ComparatorDefinition(basicComparator("="), required(), required(firstArgType()));
	}

	static ComparatorDefinition ne() {
		return new ComparatorDefinition(basicComparator("<>"), required(), required(firstArgType()));
	}
	
	static ComparatorDefinition lt() {
		return new ComparatorDefinition(basicComparator("<"), required(), required(firstArgType()));
	}

	static ComparatorDefinition le() {
		return new ComparatorDefinition(basicComparator("<="), required(), required(firstArgType()));
	}

	static ComparatorDefinition gt() {
		return new ComparatorDefinition(basicComparator(">"), required(), required(firstArgType()));
	}

	static ComparatorDefinition ge() {
		return new ComparatorDefinition(basicComparator(">="), required(), required(firstArgType()));
	}
	
	static ComparatorDefinition between() {
		return new ComparatorDefinition(rangeComparator("BETWEEN"), required(), required(firstArgType()), required(firstArgType()));
	}
	
	//string comparator
	
	static ComparatorDefinition startsLike() {
		return like(o-> o + "%");
	}

	static ComparatorDefinition endsLike() {
		return like(o-> "%" + o);
	}

	static ComparatorDefinition contentLike() {
		return like(o-> "%" + o + "%");
	}
	
	static ComparatorDefinition startsNotLike() {
		return notLike(o-> o + "%");
	}

	static ComparatorDefinition endsNotLike() {
		return notLike(o-> "%" + o);
	}

	static ComparatorDefinition contentNotLike() {
		return notLike(o-> "%" + o + "%");
	}

	static ComparatorDefinition like() {
		return like(null);
	}
	
	static ComparatorDefinition iLike() {
		return iLike(null);
	}
	
	static ComparatorDefinition notLike() {
		return notLike(null);
	}

	static ComparatorDefinition notILike() {
		return notILike(null);
	}
	
	static ComparatorDefinition like(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	static ComparatorDefinition iLike(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	static ComparatorDefinition notLike(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("NOT LIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}

	static ComparatorDefinition notILike(UnaryOperator<Object> wilcard) {
		return new ComparatorDefinition(stringComparator("NOT ILIKE", wilcard), required(VARCHAR), required(VARCHAR));
	}
	
	//null comparator
	
	static ComparatorDefinition isNull() {
		return new ComparatorDefinition(nullComparator("IS NULL"), required());
	}

	static ComparatorDefinition notNull() { //isNotNUll
		return new ComparatorDefinition(nullComparator("IS NOT NULL"), required());
	}
	
	//in comparator

	static ComparatorDefinition in() {
		return new ComparatorDefinition(inComparator("IN"), required(), required(firstArgType()), varargs(firstArgType()));
	}
	
	static ComparatorDefinition notIn() {
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
