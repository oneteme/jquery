package org.usf.jquery.core;

import static org.usf.jquery.core.Clause.FILTER;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
public interface Nested {
	
	boolean resolve(QueryBuilder builder, Consumer<? super DBColumn> cons);
	
	void views(Consumer<DBView> cons); //collect used views
	
	static void viewsOf(Consumer<DBView> cons, Object... args) { //collect used views
		if(!isEmpty(args)) {
			for(var o : args) {
				if(o instanceof Nested n) {
					n.views(cons);
				}
			}
		}
	}
	
	static boolean tryResolve(QueryBuilder builder, Consumer<? super DBColumn> cons, Object... args){
		if(!isEmpty(args)) {
			if(builder.getClause() == FILTER) {
				for(var o : args) {
					if(tryResolve(o, builder, cons)) {
						return true; //break 
					}
				}
			} //else  WhenCase filter
			var arr = new ArrayList<DBColumn>();
			var agg = false;
			for(var o : args) {
				agg |= tryResolve(o, builder, arr::add);
			}
			if(agg && !arr.isEmpty()) { //partial aggregation
				arr.forEach(cons);
			}
			return agg;
		}
		return false;
	}
	
	static boolean tryResolve(Object o, QueryBuilder builder, Consumer<? super DBColumn> cons) {
		return o instanceof Nested n && n.resolve(builder, cons);
	}
}
