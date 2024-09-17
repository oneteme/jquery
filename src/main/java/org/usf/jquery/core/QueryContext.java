package org.usf.jquery.core;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
import static org.usf.jquery.core.SqlStringBuilder.quote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	private final String vPrefix;
	private final List<Object> args; //parameterized flag
	private final List<JDBCType> argTypes;
	private final List<DBView> views; //indexed view
	private final Map<DBView, QueryView> overView;
	
	public List<DBView> views(){
		return views;
	}
	
	public String viewAlias(DBView view) {
		var idx = views.indexOf(view);
		if(idx < 0) {
			idx = views.size();
			views.add(view);
		}
		return vPrefix + (idx+1);
	}
	
	public Optional<DBView> viewOverload(DBView view) {
		return ofNullable(overView.get(view));
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
		if(o instanceof DBObject jo) {
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
		return new QueryContext(schema, vPrefix, null, null, views, overView);
	}
	
	public QueryContext subQuery(Map<DBView, QueryView> overView) { //share schema, prefix, args but not views
		return new QueryContext(schema, vPrefix + "_s", args, argTypes, new ArrayList<>(), unmodifiableMap(overView));
	}
	
	QueryContext fork() { //share schema, prefix and views, but not args
		return dynamic()
				? new QueryContext(schema, vPrefix, new ArrayList<>(), new ArrayList<>(), views, overView)
				: withValue();
	}
	
	void join(QueryContext ctx) { //join args only
		if(dynamic()) {
			args.addAll(ctx.args);
			argTypes.addAll(ctx.argTypes);
		}
	}

	public static QueryContext addWithValue() {
		return addWithValue(null, emptyMap()); //no args
	}

	public static QueryContext addWithValue(String schema, Map<DBView, QueryView> overView) {
		return new QueryContext(schema, "v", null, null, new ArrayList<>(), unmodifiableMap(overView)); //no args
	}

	public static QueryContext parameterized(String schema, Map<DBView, QueryView> overView) {
		return new QueryContext(schema, "v", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), unmodifiableMap(overView));
	}
}
