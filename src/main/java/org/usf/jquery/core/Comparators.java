package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.VARCHAR;
import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.TypeResolver.firstArgType;
import static org.usf.jquery.core.Utils.toList;

/**
 * 
 * @author u$f
 *
 */
public interface Comparators {
	
	//basic comparator
	
	default ComparatorDefinition eq() {
		return basicComparator("=", "eq");
	}

	default ComparatorDefinition ne() {
		return basicComparator("<>", "ne");
	}
	
	default ComparatorDefinition lt() {
		return basicComparator("<", "lt");
	}

	default ComparatorDefinition le() {
		return basicComparator("<=", "le");
	}

	default ComparatorDefinition gt() {
		return basicComparator(">", "gt");
	}

	default ComparatorDefinition ge() {
		return basicComparator(">=", "ge");
	}
	
	default ComparatorDefinition between() {
		return rangeComparator("BETWEEN");
	}
	
	//string comparator

	default ComparatorDefinition like() {
		return stringComparator("LIKE", "", "");
	}
	
	default ComparatorDefinition startsLike() {
		return stringComparator("LIKE", "", "%");
	}

	default ComparatorDefinition endsLike() {
		return stringComparator("LIKE", "%", "");
	}

	default ComparatorDefinition contentLike() {
		return stringComparator("LIKE", "%", "%");
	}
	
	default ComparatorDefinition notLike() {
		return stringComparator("NOT LIKE", "", "");
	}
	
	default ComparatorDefinition startsNotLike() {
		return stringComparator("NOT LIKE", "", "%");
	}

	default ComparatorDefinition endsNotLike() {
		return stringComparator("NOT LIKE", "%", "");
	}

	default ComparatorDefinition contentNotLike() {
		return stringComparator("NOT LIKE", "%", "%");
	}
	
	default ComparatorDefinition iLike() {
		return stringComparator("ILIKE", "", "");
	}

	default ComparatorDefinition notILike() {
		return stringComparator("NOT ILIKE", "", "");
	}
	
	//null comparator
	
	default ComparatorDefinition isNull() {
		return nullComparator("IS NULL");
	}

	default ComparatorDefinition notNull() { //isNotNUll
		return nullComparator("IS NOT NULL");
	}
	
	//in comparator

	default ComparatorDefinition in() {
		return inComparator("IN");
	}
	
	default ComparatorDefinition notIn() {
		return inComparator("NOT IN");
	}
	
	static ComparatorDefinition basicComparator(final String symbol, final String name) {
		return new ComparatorDefinition(name,
				(builder,args)-> builder.appendParameter(args[0]).append(symbol).appendParameter(args[1], true),
				required(), required(firstArgType()));
	}
	
	static ComparatorDefinition stringComparator(final String name, final String prefix, final String suffix) {
		return new ComparatorDefinition(name, 
				(builder,args)-> builder.appendParameter(args[0])
				.appendSpace().append(name).appendSpace()
				.appendParameter(prefix + args[1] + suffix, true), 
				required(VARCHAR), required(VARCHAR));
	}
	
	static ComparatorDefinition nullComparator(final String name) {
		return new ComparatorDefinition(name,
				(builder,args)-> builder.appendParameter(args[0]).appendSpace().append(name),
				required());
	}
	
	static ComparatorDefinition inComparator(final String name) {
		return new ComparatorDefinition(name, 
				(builder,args)-> builder.appendParameter(args[0]).appendSpace().append(name)
				.append("(").appendParameters(SqlBuilder.SCOMA, toList(args, 1), true).append(")"), 
				required(), required(firstArgType()), varargs(firstArgType()));
	}

	static ComparatorDefinition rangeComparator(final String name) {
		return new ComparatorDefinition(name, 
				(builder,args)-> builder.appendParameter(args[0])
				.appendSpace().append(name).appendSpace()
				.appendParameter(args[1], true).append(AND.sql()).appendParameter(args[2], true),
				required(), required(firstArgType()), required(firstArgType()));
	}
}
