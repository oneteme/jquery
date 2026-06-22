package org.usf.jquery.web.proxy;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Parameters.CTE_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.GROUP_PARAM;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.SELECT_PARAM;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;
import static org.usf.jquery.web.proxy.EntryEvaluators.evaluateFilter;
import static org.usf.jquery.web.proxy.EntryParser.parseEntries;
import static org.usf.jquery.web.proxy.EntryParser.parseEntry;

import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.ComposerDefinition;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.web.proxy.ViewRegistry.DataViewer;

import lombok.NonNull;

/**
 * 
 * @author u$f
 * 
 */
public interface QueryInterpreter {
	
	default MvcRequest parseQuery(StoreResource store, String dataset, @NonNull Map<String, String[]> parameterMap) {
		var ctx = store.createContext(dataset);
		var query = new QueryComposer();
		var view = parseViewer(parameterMap, VIEW_PARAM, ctx); //TD accept alias for download
		parseParam(parameterMap, CTE_PARAM, ctx.getDialect().cte(query), ctx);
		parseParam(parameterMap, SELECT_PARAM, ctx.getDialect().select(query), ctx);
		parseParam(parameterMap, JOIN_PARAM, ctx.getDialect().join(query), ctx);
		parseParam(parameterMap, GROUP_PARAM, ctx.getDialect().group(query), ctx);
		parseParam(parameterMap, ORDER_PARAM, ctx.getDialect().order(query), ctx);
		parseParam(parameterMap, DISTINCT_PARAM, ctx.getDialect().distinct(query), ctx);
		parseParam(parameterMap, LIMIT_PARAM, ctx.getDialect().limit(query), ctx);
		parseParam(parameterMap, OFFSET_PARAM, ctx.getDialect().offset(query), ctx);
//		parseParam(parameterMap, UNION_PARAM, ctx.getDialect().union(query), ctx);
		//TD parse group, from, union
		parseFilters(parameterMap, query, ctx);
		return new MvcRequest(store, query, view);
	}
	
	default DataViewer parseViewer(@NonNull Map<String, String[]> parameterMap, String param, RequestContext ctx){
		var params = parameterMap.remove(param);
		if(!isEmpty(params)) {
			if(params.length == 1) {
				return ctx.getStore().viewRegistry().viewer(params[0]);
			}
			throw new IllegalArgumentException("Parameter " + param + " can only have one value");
		}
		throw new IllegalArgumentException("require " + param + " parameter");
	}
	
	default void parseParam(Map<String, String[]> parameterMap, String param, ComposerDefinition<QueryComposer> def, RequestContext ctx) { //ctes
		var params = parameterMap.remove(param);
		if(nonNull(params)) {
			try {
				def.invoke(ctx.resolveArgs(parse(params).toArray(Entry[]::new), null, def));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + param, e);
			}
		}
	}
	
	default void parseFilters(Map<String, String[]> parameterMap, QueryComposer composer, RequestContext ctx) {
		if(!isEmpty(parameterMap)) {
			Object[] args = parameterMap.entrySet().stream()
					.map(e-> evaluateFilter(parseEntry(e.getKey()), ctx, parse(e.getValue()).toArray(Entry[]::new)))
					.toArray(Criteria[]::new);
			ctx.getDialect().criteria(composer).invoke(args);
		}
	}
	
	static String requireOneArg(String[] arr, String name) {
		if(isEmpty(arr)) {
			throw new IllegalArgumentException("require " + name + " parameter");
		}
		if(arr.length > 1) {
			throw new IllegalArgumentException("Parameter " + name + " can only have one value");
		}
		return arr[0];
	}
	
	private static Stream<Entry> parse(String[] values) {
		return stream(values).flatMap(c-> stream(parseEntries(c)));
	}
}
