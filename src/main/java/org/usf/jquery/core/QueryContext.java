package org.usf.jquery.core;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class QueryContext {
	
	private static final String P_ARG = "?";
	
	@Getter
	private final String schema;
	private final String vPrefix;
	private final List<Object> args; //parameterized flag
	private final List<JDBCType> argTypes;
	private final Map<DBView, String> views;
	private final Map<DBView, QueryView> overViews;
	private final Map<QueryView, String> globalViews; //CTE
	
	public Collection<DBView> views(){
		return views.keySet();
	}
	
	public String viewAlias(DBView view) {
		if(overViews.containsKey(view)) {
			view = overViews.get(view);
		}
		return views.computeIfAbsent(view, k-> globalViews.containsKey(k)
			? globalViews.get(k) //global alias
			: vPrefix + (views.size()+1));
	}
	
	public void appendView(SqlStringBuilder sb, DBView view){
		if(overViews.containsKey(view)) {
			view = overViews.get(view);
		}
		if(globalViews.containsKey(view)) {
			sb.append(globalViews.get(view)); //alias only
		}
		else if(views.containsKey(view)) {
			view.sqlUsingTag(sb, this); //view as alias
		}
		else {
			throw new JQueryException("view not found " + view);
		}
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
		argTypes.add(type);
		args.add(o);
		return P_ARG;
	}
	
	public Object[] args() {
		return dynamic() ? args.toArray() : null;
	}
	
	public int[] argTypes() {
		return dynamic() ? argTypes.stream().mapToInt(JDBCType::getValue).toArray() : null;
	}
	
	private boolean dynamic() {
		return nonNull(args);
	}

	static String nParameter(int n){
		if(n < 1){
			return EMPTY;
		}
        return n == 1 ? P_ARG : P_ARG + (COMA + P_ARG).repeat(n-1);
    }

	public static String formatValue(Object o) {
		if(o instanceof Number){
			return o.toString();
		}
		return nonNull(o) ? quote(o.toString()) : "null";
	}
	
	public QueryContext withValue() {
		return new QueryContext(schema, vPrefix, null, null, views, overViews, globalViews);
	}
	
	public QueryContext subQuery(Map<DBView, QueryView> overViews) { //share schema, prefix, args but not views
		return new QueryContext(schema, vPrefix + "_s", args, argTypes, new HashMap<>(), overViews, globalViews);
	}
	
	QueryContext fork() { //share schema, prefix and views, but not args
		return dynamic()
				? new QueryContext(schema, vPrefix, new ArrayList<>(), new ArrayList<>(), views, overViews, globalViews)
				: withValue();
	}
	
	void join(QueryContext ctx) { //join args only
		if(dynamic() && ctx.dynamic()) {
			args.addAll(ctx.args);
			argTypes.addAll(ctx.argTypes);
		}
		else if(dynamic() ^ ctx.dynamic()){
			throw new IllegalStateException("not same");
		}
	}

	public static QueryContext addWithValue() {
		return addWithValue(null, emptyList(), emptyMap()); //no args
	}

	public static QueryContext addWithValue(String schema, Collection<QueryView> globalViews, Map<DBView, QueryView> overView) {
		return of(schema, "v", null, null, globalViews, overView); //no args
	}

	public static QueryContext parameterized(String schema, Collection<QueryView> globalViews, Map<DBView, QueryView> overView) {
		return of(schema, "v", new ArrayList<>(), new ArrayList<>(), globalViews, overView);
	}
	
	private static QueryContext of(String schema, String vPrefix, List<Object> args, List<JDBCType> argTypes, Collection<QueryView> globalViews, Map<DBView, QueryView> overViews) {
		var globMap = new HashMap<QueryView, String>();
		if(!isEmpty(globalViews)) {
			for(var v : globalViews) {
				globMap.put(v, "g"+ (globMap.size()+1));
			}
		}
		return new QueryContext(schema, vPrefix, args, argTypes, new HashMap<>(), unmodifiableMap(overViews), unmodifiableMap(globMap));
	}
}
