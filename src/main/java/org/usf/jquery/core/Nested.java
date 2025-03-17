package org.usf.jquery.core;

import static java.lang.Math.max;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 
 * @author u$f
 *
 */
public interface Nested {
	
	static final Consumer<DBColumn> DO_NOTHING = o-> {};
	
	//0: groupKey, +1: aggregation, -1: other  
	int columns(QueryBuilder builder, Consumer<DBColumn> cons);
	
	void views(Consumer<DBView> cons); //collect used views
	
	static int resolveColumn(DBColumn col, QueryBuilder builder, Consumer<DBColumn> cons, Nested[] args){
		var arr = new ArrayList<DBColumn>();
		var lvl = resolveColumn(builder, arr::add, args);
		return tryResolveColumn(col, arr, lvl, cons);
	}
	
	static int resolveColumn(QueryBuilder builder, Consumer<DBColumn> cons, Nested[] args){
		var lvl = -1;
		if(!isEmpty(args)) {
			for(var o : args) {
				lvl = max(lvl, o.columns(builder, cons));
			}
		}
		return lvl;
	}
	
	static int tryResolveColumn(DBColumn col, QueryBuilder builder, Consumer<DBColumn> cons, Object... args){
		var arr = new ArrayList<DBColumn>();
		var lvl = tryResolveColumn(builder, arr::add, args);
		return tryResolveColumn(col, arr, lvl, cons);
	}
	
	private static int tryResolveColumn(DBColumn col, List<DBColumn> subColumns, int lvl, Consumer<DBColumn> cons){
		if(lvl == 0) { //group keys
			cons.accept(col);
		}
		else if(lvl > 0 && !subColumns.isEmpty()) {
			subColumns.forEach(cons);
		}
		return lvl;
	}
	
	static int tryResolveColumn(QueryBuilder builder, Consumer<DBColumn> cons, Object... args){
		var lvl = -1;
		if(!isEmpty(args)) {
			for(var o : args) {
				if(o instanceof Nested n) {
					lvl = max(lvl, n.columns(builder, cons));
				}
			}
		}
		return lvl;
	}
	
	static void resolveViews(Consumer<DBView> cons, Nested[] args) { //collect used views
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
