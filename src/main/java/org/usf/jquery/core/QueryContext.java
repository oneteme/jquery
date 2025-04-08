package org.usf.jquery.core;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ObjIntConsumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryContext {
	
	private static final String P_ARG = "?";
	
	@Getter
	private final String schema;
	private final String prefix;
	private final Map<DBView, String> views;
	private final List<QueryArg> args;
	
	private final Collection<QueryView> ctes; //only for sub query
	
	public Collection<DBView> views(){
		return views.keySet();
	}
	
	public String viewAlias(DBView view) {
		return views.get(view);
	}
	
	public void viewProxy(DBView view, QueryView query) {
		views.put(view, views.get(query)); //add or replace
	}
	
	public void appendArrayParameter(SqlStringBuilder sb, Object[] arr) {
		appendArrayParameter(sb, arr, 0);
	}
	
	public void appendArrayParameter(SqlStringBuilder sb, Object[] arr, int from) {
		if(dynamic()) {
			sb.runForeach(arr, from, SCOMA, o-> appendParameter(sb, o));
		}
		else {
			appendLiteralArray(sb, arr, from);
		}
	}

	public void appendLiteralArray(SqlStringBuilder sb, Object[] arr) {
		appendLiteralArray(sb, arr, 0);
	}
	
	public void appendLiteralArray(SqlStringBuilder sb, Object[] arr, int from) {
		sb.runForeach(arr, from, SCOMA, o-> appendLiteral(sb, o));
	}

	public void appendParameter(SqlStringBuilder sb, Object o) {
		if(dynamic()) {
			if(o instanceof DBObject jo) {
				jo.sql(sb, this, null);
			}
			else {
				var t = typeOf(o);
				sb.append(t.isPresent() ? appendArg(t.get(), o) : formatValue(o));
			}
		}
		else {
			appendLiteral(sb, o);
		}
	}

	public void appendLiteral(SqlStringBuilder sb, Object o) {  //TD : stringify value using db default pattern
		if(o instanceof DBObject jo) { //DBColumn | QueryView
			jo.sql(sb, this, null);
		}
		else {
			sb.append(formatValue(o));
		}
	}
		
	private String appendArg(JDBCType type, Object o) {
		args.add(new QueryArg(o, type.getValue()));
		return P_ARG;
	}
	
	public QueryArg[] args() {
		return dynamic() ? args.toArray(QueryArg[]::new) : null;
	}
	
	private boolean dynamic() {
		return nonNull(args);
	}

	public static String formatValue(Object o) {
		if(o instanceof Number){
			return o.toString();
		}//else String|Date
		return nonNull(o) ? quote(o.toString()) : "null";
	}
	
	public QueryContext withValue() {
		return new QueryContext(schema, prefix, views, null, null); //no args
	}
	
	public QueryContext subQuery(Collection<DBView> views) { //share schema, prefix, args but not views
		return new QueryContext(schema, prefix + "_s", toLinkedMap(ctes, views), args, ctes);
	}

	public static QueryContext addWithValue() {
		return addWithValue(null, emptyList(), emptyList()); //no args
	}

	public static QueryContext addWithValue(String schema, Collection<QueryView> ctes, Collection<DBView> views) {
		return new QueryContext(schema, "v", toLinkedMap(ctes, views), null, unmodifiableCollection(ctes));
	}

	public static QueryContext parameterized(String schema, Collection<QueryView> ctes, Collection<DBView> views) {
		return new QueryContext(schema, "v", toLinkedMap(ctes, views), new ArrayList<>(), unmodifiableCollection(ctes));
	}
	
	private static Map<DBView, String> toLinkedMap(Collection<QueryView> ctes, Collection<DBView> views){
		var map = new HashMap<DBView, String>();
		doForeach(ctes,  (v,i)-> map.put(v, "g"+i));
		doForeach(views, (v,i)-> map.put(v, "v"+i));
		return map; //modifiable map
	}
	
	private static <T> void doForeach(Collection<T> views, ObjIntConsumer<? super T> cons) {
		if(!isEmpty(views)) {
			int i=0;
			for(var v : views) {
				cons.accept(v, i);
			}
		}
	}
}
