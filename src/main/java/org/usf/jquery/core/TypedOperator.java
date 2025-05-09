package org.usf.jquery.core;

import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class TypedOperator implements Operator {

	//do not @Delegate
	private final Operator operator;
	private final ArgTypeRef typeFn;
	private final ParameterSet parameterSet;
	
	public TypedOperator(JDBCType type, Operator function, Parameter... parameter) {
		this(o-> type, function, parameter);
	}

	public TypedOperator(ArgTypeRef typeFn, Operator function, Parameter... parameter) {
		this.operator = function;
		this.typeFn = typeFn;
		this.parameterSet = ofParameters(parameter);
	}
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		try {
			operator.build(query, parameterSet.assertArguments(args));
		}
		catch (BadArgumentException e) {
			throw badArgumentsException("operator", operator.id(), args, e);
		}
	}
	
	@Override
	public String id() {
		return operator.id();
	}

	public DBColumn operation(Object... args) {
		return this.operation(typeFn.apply(args), args);
	}

	@Override // do not delegate this
	public DBColumn operation(JDBCType type, Object... args) {
		return operator.is(CombinedOperator.class)
				? operator.operation(type, parameterSet.assertArguments(args)) //no sql
				: Operator.super.operation(type, args); //TODO check that
	}
	
	@Override
	public boolean is(Class<? extends Operator> type) {
		return operator.is(type);
	}
	
	@Override
	public boolean is(String name) {
		return operator.is(name);
	}
	
	public boolean isWindowFunction() {
		return operator.is(WindowFunction.class);
	}
	
	public boolean isCountFunction() {
		return operator.is("COUNT");
	}
	
	@Override
	public String toString() {
		return operator.id() + parameterSet.toString();
	}
}
