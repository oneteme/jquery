package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.empty;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
public interface Nested {
	
	int compose(QueryComposer composer, Consumer<DBColumn> groupKeys);
	
	static int aggregation(QueryComposer query, Consumer<DBColumn> cons, Nested[] args){
		return aggregation(query, cons, null, args);
	}
	
	static int aggregation(QueryComposer query, Consumer<DBColumn> cons, DBColumn col, Nested[] args){
		return resolveAggragationColumns(query, cons, streamOrEmptry(args), col);
	}
	
	static int tryAggregation(QueryComposer query, Consumer<DBColumn> cons, Object... args){
		return tryAggregation(query, cons, null, args);
	}
	
	static int tryAggregation(QueryComposer query, Consumer<DBColumn> cons, DBColumn col, Object... args){
		return resolveAggragationColumns(query, cons, streamOrEmptry(args).mapMulti((o, acc)->{
			if(o instanceof Nested n) {
				acc.accept(n);
			}
		}), col);
	}

	//0: groupKey, +1: aggregation, -1: constant  
	private static int resolveAggragationColumns(QueryComposer query, Consumer<DBColumn> cons, Stream<Nested> stream, DBColumn col){
		if(isNull(col) || isNull(cons)) { //declare only
			return stream.mapToInt(o-> o.compose(query, cons)).max().orElse(-1);
		}
		var arr = new ArrayList<DBColumn>();
		var lvl = stream.mapToInt(o-> o.compose(query, arr::add)).max().orElse(-1);
		if(lvl == 0) { //group keys
			cons.accept(col);
		}
		else if(lvl > 0 && !arr.isEmpty()) {
			arr.forEach(cons);
		}
		return lvl;
	}
	
	private static <T> Stream<T> streamOrEmptry(T[] arr) {
		return nonNull(arr) ? stream(arr) : empty();
	}
}
