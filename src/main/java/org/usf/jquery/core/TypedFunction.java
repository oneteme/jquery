package org.usf.jquery.core;

import static java.util.stream.Stream.concat;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
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
public class TypedFunction implements DBFunction {
	
	@Delegate
	private final DBFunction fn;
	private final int[] argTypes;
	@Getter
	private final int returnedType;
	
	private Object[] additionalArgs;
	
	public TypedFunction(DBFunction fn) {
		this(AUTO_TYPE, fn);
	}
	
	public TypedFunction(int returnedType, DBFunction fn, int... argTypes) {
		this.returnedType = returnedType;
		this.fn = fn;
		this.argTypes = argTypes;
	}
	
	public TypedFunction usingArgs(Object... args) {
		this.additionalArgs = args;
		return this;
	}
	
	//do not delegate this
	public OperationColumn args(Object... args) {
		return DBFunction.super.args(args);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		args = mergeArrays(args, this.additionalArgs);
		return fn.sql(builder, isEmpty(argTypes) 
				? requireNoArgs(args, fn::name)
				: requireNArgs(argTypes.length, args, fn::name), i-> argTypes[i]);
	}
	
	public Class<? extends DBFunction> functionType() {
		return fn.getClass();
	}
	
	public static TypedFunction autoTypeReturn(DBFunction fn, int... argTypes) {
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