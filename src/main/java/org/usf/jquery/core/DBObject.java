package org.usf.jquery.core;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.empty;
import static org.usf.jquery.core.QueryBuilder.addWithValue;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
public interface DBObject {
	
	int compose(QueryComposer composer, Consumer<DBColumn> groupKeys);
	
	void build(QueryBuilder query, Object... args);

	static int composeNested(QueryComposer query, Consumer<DBColumn> cons, DBObject[] args){
		return composeNested(query, cons, null, args);
	}
	
	static int composeNested(QueryComposer query, Consumer<DBColumn> cons, DBColumn col, DBObject[] args){
		return composeNested(query, cons, streamOrEmptry(args), col);
	}

	static int tryComposeNested(QueryComposer query, Consumer<DBColumn> cons, Object... args){
		return tryComposeNested(query, cons, null, args);
	}

	static int tryComposeNested(QueryComposer query, Consumer<DBColumn> cons, DBColumn col, Object... args){
		return composeNested(query, cons, streamOrEmptry(args).mapMulti((o, acc)->{
			if(o instanceof DBObject n) {
				acc.accept(n);
			}
		}), col);
	}

	//0: groupKey, +1: aggregation, -1: constant  
	static int composeNested(QueryComposer query, Consumer<DBColumn> cons, Stream<DBObject> stream, DBColumn col){
		if(isNull(col)) { //declare only
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

	static String toSQL(DBObject obj, Object... args) {
		var query = addWithValue();
		obj.build(query, args);
		return query.build().getSql();
	}
	
	private static <T> Stream<T> streamOrEmptry(T[] arr) {
		return nonNull(arr) ? stream(arr) : empty();
	}
}
