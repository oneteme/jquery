package org.usf.jquery.core;

import static org.usf.jquery.core.Parameter.checkArgs;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Function;

import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class TypedOperator implements Operator {

	@Delegate
	private final Operator operator;
	private final Function<Object[], JavaType> typeFn;
	private final Parameter[] parameters;
	
	public TypedOperator(JavaType type, Operator function, Parameter... args) {
		this(o-> type, function, args == null ? new Parameter[0] : args);
	}

	public TypedOperator(Function<Object[], JavaType> typeFn, Operator function, Parameter... parameter) {
		this.typeFn = typeFn;
		this.operator = function;
		this.parameters = checkArgs(parameter);
	}
	
	public Operator unwrap() {
		return operator;
	}
	
	@Override
	public OperationColumn args(Object... args) {
		// TODO check arg types
		if(isEmpty(parameters)) {
			requireNoArgs(args, operator::id);
		}
		else {
			if(isEmpty(args)) {
				if(parameters[0].isRequired()) {
					
				}
				//require args
			}
			else {
				
			}
		}
		return new OperationColumn(operator, args, typeFn.apply(args));
	}
	
	public int requireArgCount() {
		var i=0;
		while(i<parameters.length && parameters[i++].isRequired());
		return i;
	}
	
	public boolean isVarags() {
		return parameters.length > 0 && parameters[parameters.length-1].isVarargs(); 
	}
}
