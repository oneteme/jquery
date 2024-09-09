package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 
 * @author u$f
 *
 */
public interface Nested {

	boolean resolve(QueryBuilder builder);
	
	void views(Collection<DBView> views);

	static boolean tryResolveAll(QueryBuilder builder, Object... args){
		return resolveAll(args, o-> tryResolve(o, builder));
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
	
	static boolean tryResolve(Object o, QueryBuilder builder) {
		return o instanceof Nested n && n.resolve(builder);
	}
	

	static <T extends Nested> void viewsOfNested(Collection<DBView> views, T[] arr) {
		viewsOfAll(arr, o-> o.views(views));
	}
	
	static <T, N extends Nested> void viewsOfNested(Collection<DBView> views, T[] arr, Function<T, N> fn) {
		viewsOfAll(arr, o-> fn.apply(o).views(views));
	}

	static void viewsOfAll(Collection<DBView> views, Object[] arr) {
		viewsOfAll(arr, o-> viewsOf(views, o));
	}

	static <T> void viewsOfAll(T[] arr, Consumer<? super T> cons) {
		if(!isEmpty(arr)) {
			for(var o : arr) {
				cons.accept(o);
			}
		}
	}

	static void viewsOf(Collection<DBView> views, Object o) {
		if(o instanceof Nested n) {
			n.views(views);
		}
	}
}
