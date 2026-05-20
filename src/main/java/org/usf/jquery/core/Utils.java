package org.usf.jquery.core;

import static java.lang.System.arraycopy;
import static java.lang.reflect.Array.newInstance;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

	public static final int UNLIMITED = -1;
	
	public static <T> boolean isEmpty(T[] a) {
		return isNull(a) || a.length == 0;
	}
	
	public static boolean isEmpty(Collection<?> c) {
		return isNull(c) || c.isEmpty();
	}
	
	public static boolean isEmpty(Map<?,?> c) {
		return isNull(c) || c.isEmpty();
	}

	public static boolean isEmpty(String s) {
		return isNull(s) || s.isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> String joinArray(String delemiter, T... args) {
		return joinAndDelemitArray(delemiter, "", "", args);
	}

	@SuppressWarnings("unchecked")
	public static <T> String joinAndDelemitArray(String delemiter, String before, String after, T... args) {
		return isNull(args) 
				? before + after 
				: joinAndDelemit(delemiter, before, after, Stream.of(args));
	}
	
	public static <T> String join(String delemiter, Stream<T> args) {
		return joinAndDelemit(delemiter, "", "", args);
	}
	
	public static <T> String joinAndDelemit(String delemiter, String before, String after, Stream<T> args) {
		return args.map(Object::toString).collect(joining(delemiter, before, after));
	}

	public static Object[] appendFirst(Object[] arr, Object o) {
		var res = new Object[1+ (nonNull(arr) ? arr.length : 0)];
		res[0] = o;
		if(nonNull(arr)) {
			arraycopy(arr, 0, res, 1, arr.length);
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] appendLast(T[] arr, T o) {
		var res = (T[])newInstance(arr.getClass().getComponentType(), arr.length+1);
		arraycopy(arr, 0, res, 0, arr.length);
		res[res.length-1] = o;
		return res;
	}
	
	public static <K, V> BiFunction<K, V, V> computeIfAbsentElseThrow(V o, Supplier<String> exceptionSupplier) {
		return (k,v)-> {
			if(isNull(v)) {
				return o;
			}
			throw new IllegalArgumentException(exceptionSupplier.get());
		};
	}
	
    public static <T, E extends Throwable> T requireNonNullElseThrow(T obj, Supplier<E> exceptionSupplier) throws E {
        if(nonNull(obj)) {
			return obj;
		}
		throw exceptionSupplier.get();
    }
    
    @SafeVarargs
    public static <T> List<T> toList(T... arr) {
    	return isEmpty(arr) ? emptyList() : asList(arr);
    }
    
	public static <T> List<T> toList(T[] arr, int from) {
    	if(!isEmpty(arr)) {
    		var list = asList(arr);
    		return from == 0 ? list : list.subList(from, arr.length);
    	}
		return emptyList();
	}
}
