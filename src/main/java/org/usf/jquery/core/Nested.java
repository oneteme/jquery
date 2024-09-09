package org.usf.jquery.core;

import static java.util.function.Function.identity;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 
 * @author u$f
 *
 */
public interface Nested {
	
	void views(Collection<DBView> views);

	boolean resolve(QueryBuilder builder);


	static boolean tryResolveAll(QueryBuilder builder, Object... args){
		return resolveAll(args, o-> resolve(o, builder));
	}

	static <T, N extends Nested> boolean resolveAll(T[] arr, Function<T, N> fn, QueryBuilder builder){
		return resolveAll(arr, o-> fn.apply(o).resolve(builder));
	}

	static boolean resolveAll(Nested[] arr, QueryBuilder builder){
		return resolveAll(arr, n-> n.resolve(builder));
	}
	
	static <T> boolean resolveAll(T[] arr, Predicate<T> fn){
		var res = false;
		if(!isEmpty(arr)) {
			for(var o : arr) {
				res |= fn.test(o);
			}
		}
		return res;
	}
	
	static boolean resolve(Object o, QueryBuilder builder) {
		return o instanceof Nested n && n.resolve(builder);
	}
	
	

	static <T extends Nested> void viewsOfNested(Collection<DBView> views, T[] arr) {
		viewsOfNested(views, arr, identity());
	}
	
	static <T, N extends Nested> void viewsOfNested(Collection<DBView> views, T[] arr, Function<T, N> fn) {
		if(!isEmpty(arr)) {
			for(var o : arr) {
				fn.apply(o).views(views);
			}
		}
	}

	static void viewsOfAll(Collection<DBView> views, Object[] arr) {
		if(!isEmpty(arr)) {
			for(var o : arr) {
				viewsOf(views, o);
			}
		}
	}

	static void viewsOf(Collection<DBView> views, Object o) {
		if(o instanceof Nested n) {
			n.views(views);
		}
	}
}
