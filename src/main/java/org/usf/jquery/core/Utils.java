package org.usf.jquery.core;

import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

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

	public static Object[] appendFirst(Object[] arr, Object o) {
		var res = new Object[1+ (nonNull(arr) ? arr.length : 0)];
		res[0] = o;
		if(nonNull(arr)) {
			arraycopy(arr, 0, res, 1, arr.length);
		}
		return res;
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
