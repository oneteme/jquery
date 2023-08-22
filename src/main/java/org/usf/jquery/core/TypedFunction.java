package org.usf.jquery.core;

import static java.util.stream.Stream.concat;
import static org.usf.jquery.core.JDBCType.AUTO_TYPE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.stream.Stream;

import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class TypedFunction implements DBFunction {
	
	@Delegate
	private final DBFunction fn;
	private final SQLType[] argTypes;
	private final SQLType returnedType;
	
	private Object[] additionalArgs;
	
	public TypedFunction(SQLType returnedType, DBFunction fn, SQLType... argTypes) {
		this.returnedType = returnedType;
		this.fn = fn;
		this.argTypes = argTypes;
	}
	
	public TypedFunction additionalArgs(Object... args) {
		this.additionalArgs = args;
		return this;
	}
	
	@Override //do not delegate this
	public OperationColumn args(Object... args) {
		return DBFunction.super.args(args);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		args = mergeArrays(args, additionalArgs);
		return fn.sql(builder, isEmpty(argTypes) 
				? requireNoArgs(args, fn::name)
				: requireNArgs(argTypes.length, args, fn::name), i-> argTypes[i]);
	}
	
	public int argumentCount() {
		return isEmpty(argTypes) ? 0 : argTypes.length;
	}
	
	public boolean isWindowFunction() {
		return fn instanceof WindowFunction;
	}
	
	public static TypedFunction autoTypeReturn(DBFunction fn, SQLType... argTypes) {
		return new TypedFunction(AUTO_TYPE, fn, argTypes);
	}
	
	private static Object[] mergeArrays(Object[] a1, Object[] a2) {
		if(isEmpty(a1)) {
			return a2;
		}
		if(isEmpty(a2)) {
			return a1;
		}
		return concat(Stream.of(a1), Stream.of(a2)).toArray();
	}
}