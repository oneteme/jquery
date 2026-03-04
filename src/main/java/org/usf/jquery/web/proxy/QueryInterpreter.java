package org.usf.jquery.web.proxy;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.concat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.CRITERIA_OPR;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.FIELD_PARAM;
import static org.usf.jquery.web.Parameters.FILTER_OPR;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;
import static org.usf.jquery.web.proxy.EntryEvaluators.evaluateFilter;
import static org.usf.jquery.web.proxy.EntryParser.parseEntries;
import static org.usf.jquery.web.proxy.EntryParser.parseEntry;
import static org.usf.jquery.web.proxy.StoreManager.getInstance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.web.ResourceAccessException;

import lombok.NonNull;

public interface QueryInterpreter {
	
	static final Logger log = getLogger(QueryInterpreter.class);
	
	default QueryComposer parseQuery(@NonNull QueryRequest qr, @NonNull Map<String, String[]> parameterMap) {
		var modifiableMap = new LinkedHashMap<>(parameterMap); //modifiable map + preserve order
		if(!isEmpty(qr.ignore())) {
			for(var k : qr.ignore()) {
				if(modifiableMap.containsKey(k)) {
					log.debug("ignoring parameter '{}' as specified in @QueryRequest", k);
					modifiableMap.remove(k);
				}
			}
		}
		resolveColumnCompatibility(modifiableMap);
		modifiableMap.computeIfAbsent(FIELD_PARAM, k-> qr.fields());		
		var store = qr.store() == StoreResource.class 
				? getInstance().getDefaultStore() //default schema if not specified
				: getInstance().getStore(qr.store());
		var query = parseQuery(modifiableMap, store.createContext(qr.dataset()));
		query.maxRows(qr.maxSize());
		if(!qr.aggregate() || query.isAggregation()) {
			return query; 
		}
		throw new ResourceAccessException("query is not aggregation");
	}
	
	default QueryComposer parseQuery(Map<String, String[]> parameterMap, RequestContext ctx) {
		var query = new QueryComposer();
		parseViews(parameterMap.remove(VIEW_PARAM), query, ctx);
		parseColumns(parameterMap.remove(FIELD_PARAM), query, ctx);
		parseJoins(parameterMap.remove(JOIN_PARAM), query, ctx);
		parseOrders(parameterMap.remove(ORDER_PARAM), query, ctx);
		parseDistinct(parameterMap.remove(DISTINCT_PARAM), query, ctx);
		parseLimit(parameterMap.remove(LIMIT_PARAM), query, ctx);
		parseOffset(parameterMap.remove(OFFSET_PARAM), query, ctx);
		parseFilters(parameterMap, query, ctx);
		return query;
	}
	
	default void parseViews(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, DBView.class))
				.<QueryView>mapMulti((v,cons)-> {
					if(v instanceof QueryView q) {
						cons.accept(q);
					}
				}).forEach(query::ctes);
		}
	}
	
	default void parseColumns(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, NamedColumn.class)).forEach(query::columns);
		}
		else {
			throw new IllegalArgumentException("Missing required parameter: " + FIELD_PARAM);
		}
	}
	
	default void parseOrders(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, Order.class)).forEach(query::orders);
		}
	}
	
	default void parseJoins(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, JoinsClause.class)).forEach(query::joins2);
		}
	}
	
	default void parseFilters(Map<String, String[]> parameterMap, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameterMap)) {
			parameterMap.entrySet().forEach(e-> 
				query.filters(evaluateFilter(parseEntry(e.getKey()), ctx, parse(e.getValue()).toArray(Entry[]::new))));
		}
	}
	
	default void parseDistinct(String[] parameters, QueryComposer query, RequestContext ctx) {
		var arg = optionalSingleArgument(parameters, DISTINCT_PARAM);
		if(!isEmpty(arg)) {
			try {
				query.distinct(ctx.parseValue(arg, Boolean.class));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + DISTINCT_PARAM, e);
			}
		}
	}
	
	default void parseLimit(String[] parameters, QueryComposer query, RequestContext ctx) {
		var arg = optionalSingleArgument(parameters, LIMIT_PARAM);
		if(!isEmpty(arg)) {
			try {
				query.limit(ctx.parseValue(arg, Integer.class));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + LIMIT_PARAM, e);
			}
		}
	}
	
	default void parseOffset(String[] parameters, QueryComposer query, RequestContext ctx) {
		var arg = optionalSingleArgument(parameters, OFFSET_PARAM);
		if(!isEmpty(arg)) {
			try {
				query.offset(ctx.parseValue(arg, Integer.class));	
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + OFFSET_PARAM, e);
			}
		}
	}
	
	private static String optionalSingleArgument(String[] param, String paramName) {
		if(nonNull(param)) {
			if(param.length==1) {
				return param[0];
			}
			if(param.length > 1) {
				throw new IllegalArgumentException("multiple values provided for parameter: " + paramName);
			}
		}
		return null;
	}
	
	private static Stream<Entry> parse(String[] values) {
		return stream(values).flatMap(c-> stream(parseEntries(c)));
	}
	
	private static void resolveColumnCompatibility(Map<String, String[]> modifiableMap) {
		Map.of(COLUMN_PARAM, FIELD_PARAM, FILTER_OPR, CRITERIA_OPR).entrySet().forEach(e-> {
			var args = modifiableMap.remove(e.getKey());
			if(!isEmpty(args)) {
				log.warn("'{}' parameter is deprecated, use {} instead", e.getKey(), e.getValue());
				modifiableMap.compute(e.getValue(), (k, v)-> isEmpty(v) 
						? args 
						: concat(stream(v), stream(args)).toArray(String[]::new));
			}
		});
	}
}
