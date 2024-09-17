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
	
	static final Consumer<Object> DO_NOTHING = o-> {};
	
	//0: groupKey, +1: aggregation, -1: other  
	int columns(QueryBuilder builder, Consumer<? super DBColumn> cons);
	
	void views(Consumer<DBView> cons); //collect used views

	static <T extends Nested> int resolveColumn(QueryBuilder builder, Consumer<? super DBColumn> cons, T[] args){
		var lvl = -1;
		if(!isEmpty(args)) {
			for(var n : args) {
				lvl = max(lvl, n.columns(builder, cons));
			}
		}
		return lvl;
	}
	
	static int tryResolveColumn(QueryBuilder builder, Consumer<? super DBColumn> cons, Object... args){
		var lvl = -1;
		if(!isEmpty(args)) {
			for(var o : args) {
				lvl = max(lvl, o instanceof Nested n ? n.columns(builder, cons) : -1);
			}
		}
		return lvl;
	}
	
	static <T extends Nested> void resolveViews(Consumer<DBView> cons, T[] args) { //collect used views
		if(!isEmpty(args)) {
			for(var n : args) {
				n.views(cons);
			}
		}
	}
	
	static void tryResolveViews(Consumer<DBView> cons, Object... args) { //collect used views
		if(!isEmpty(args)) {
			for(var o : args) {
				if(o instanceof Nested n) {
					n.views(cons);
				}
			}
		}
	}
}
