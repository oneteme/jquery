package org.usf.jquery.web.proxy;

import java.lang.reflect.Parameter;
import java.util.function.Function;

import org.usf.jquery.core.InvocationException;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class MethodInvoker<T> {
	
	private static final Parameter[] NO_PARAM = new Parameter[0];
	
	private final boolean accessible;
	private final Parameter[] parameters;
	private final Function<Object[], T> invoker;
	
	public MethodInvoker(boolean accessible, Function<Object[], T> invoker) {
		this.accessible = accessible;
		this.invoker = invoker;
		this.parameters = NO_PARAM;
	}
	
	public boolean isAccessible() {
		return accessible;
	}
	
	public Parameter[] getParameters() {
		return parameters;
	}
	
	public T invoke(Object... args) {
		if(accessible) {
			return invoker.apply(args);
		}
		throw new InvocationException("resource not accessible");
	}
}
