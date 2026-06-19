package org.usf.jquery.web.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.usf.jquery.core.InvocationException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class ResourceInvoker<T> {
	
	private static final Class<?>[] NO_PARAM = new Class<?>[0];
	
	@Getter private final boolean accessible;
	@Getter private final Class<?> type;
	@Getter private final Class<?>[] parameters;
	private final Function<Object[], T> invoker;
	
	public T invoke(Object... args) {
		if(accessible) {
			return invoker.apply(args);
		}
		throw new InvocationException("resource not accessible");
	}
	
	public static <T> ResourceInvoker<T> ofObject(boolean accessible, T target, Class<? super T> type) {
		return new ResourceInvoker<>(accessible, type, NO_PARAM, arr-> target);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ResourceInvoker<T> ofMethod(boolean accessible, Method mth, Object origin) {
		return new ResourceInvoker<>(accessible, mth.getReturnType(), mth.getParameterTypes(), arr->{
			try {
				return (T) mth.invoke(origin, arr);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				throw new InvocationException(e);
			}
		});
	}
}
