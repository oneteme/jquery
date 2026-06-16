package org.usf.jquery.web.proxy;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.concat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.CRITERIA_OPR;
import static org.usf.jquery.web.Parameters.CTE_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.FILTER_OPR;
import static org.usf.jquery.web.Parameters.GROUP_PARAM;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.SELECT_PARAM;
import static org.usf.jquery.web.Parameters.UNION_PARAM;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;
import static org.usf.jquery.web.proxy.EntryEvaluators.evaluateFilter;
import static org.usf.jquery.web.proxy.EntryParser.parseEntries;
import static org.usf.jquery.web.proxy.EntryParser.parseEntry;
import static org.usf.jquery.web.proxy.StoreManager.getInstance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.usf.jquery.core.ComposerDefinition;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.QueryComposer;
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
		resolveParameterCompatibility(modifiableMap);
		modifiableMap.computeIfAbsent(SELECT_PARAM, k-> qr.fields());		
		var store = qr.store() == StoreResource.class 
				? getInstance().getDefaultStore() //default schema if not specified
				: getInstance().getStore(qr.store());
		var query = parseQuery(modifiableMap, store.createContext(qr.dataset(), Set.of(qr.excludeViews()), Set.of(qr.excludeResources()), Set.of(qr.excludeDialects())));
		query.maxRows(qr.maxSize());
		if(!qr.aggregate() || query.isAggregation()) {
			return query; 
		}
		throw new ResourceAccessException("query is not aggregation");
	}
	
	default QueryComposer parseQuery(Map<String, String[]> parameterMap, RequestContext ctx) {
		var query = new QueryComposer();
		parseParam(parameterMap, CTE_PARAM, ctx.getDialect().cte(query), ctx);
		parseParam(parameterMap, SELECT_PARAM, ctx.getDialect().select(query), ctx);
		parseParam(parameterMap, JOIN_PARAM, ctx.getDialect().join(query), ctx);
		parseParam(parameterMap, GROUP_PARAM, ctx.getDialect().group(query), ctx);
		parseParam(parameterMap, ORDER_PARAM, ctx.getDialect().order(query), ctx);
		parseParam(parameterMap, DISTINCT_PARAM, ctx.getDialect().distinct(query), ctx);
		parseParam(parameterMap, LIMIT_PARAM, ctx.getDialect().limit(query), ctx);
		parseParam(parameterMap, OFFSET_PARAM, ctx.getDialect().offset(query), ctx);
//		parseParam(parameterMap, UNION_PARAM, ctx.getDialect().union(query), ctx);
		//TODO parse group, from, union
		parseFilters(parameterMap, query, ctx);
		return query;
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
	
	private static Stream<Entry> parse(String[] values) {
		return stream(values).flatMap(c-> stream(parseEntries(c)));
	}
	
	private static void resolveParameterCompatibility(Map<String, String[]> modifiableMap) {
		Map.of(COLUMN_PARAM, SELECT_PARAM, FILTER_OPR, CRITERIA_OPR, VIEW_PARAM, CTE_PARAM).entrySet().forEach(e-> {
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
