package org.usf.jquery.core;

import static java.lang.Math.max;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
public interface Nested {
	
	//0: groupKey, +1: aggregation, -1: other  
	int resolve(QueryBuilder builder, Consumer<? super DBColumn> cons);
	
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

	static <T extends Nested> int resolve(QueryBuilder builder, Consumer<? super DBColumn> cons, T[] args){
		var lvl = -1;
		if(!isEmpty(args)) {
			for(var o : args) {
				lvl = max(lvl, o.resolve(builder, cons));
			}
		}
		return lvl;
	}
	
	static int tryResolve(QueryBuilder builder, Consumer<? super DBColumn> cons, Object... args){
		var lvl = -1;
		if(!isEmpty(args)) {
			for(var o : args) {
				lvl = max(lvl, o instanceof Nested n ? n.resolve(builder, cons) : -1);
			}
		}
		return lvl;
	}
}
