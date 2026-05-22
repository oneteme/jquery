package org.usf.jquery.web.proxy;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.FIELD_PARAM;
import static org.usf.jquery.web.Parameters.GROUP_PARAM;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.SELECT_OPR;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;
import static org.usf.jquery.web.proxy.EntryEvaluators.evaluateFilter;
import static org.usf.jquery.web.proxy.EntryEvaluators.evaluateView;
import static org.usf.jquery.web.proxy.EntryParser.parseEntries;
import static org.usf.jquery.web.proxy.EntryParser.parseEntry;
import static org.usf.jquery.web.proxy.StoreManager.getInstance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.Query;
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
		modifiableMap.computeIfAbsent(SELECT_OPR, k-> qr.fields());		
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
		parseViews(parameterMap.remove(VIEW_PARAM), query, ctx);
		parseColumns(parameterMap.remove(SELECT_OPR), query, ctx);
		parseJoins(parameterMap.remove(JOIN_PARAM), query, ctx);
		parseGroups(parameterMap.remove(GROUP_PARAM), query, ctx);
		parseOrders(parameterMap.remove(ORDER_PARAM), query, ctx);
		parseDistinct(parameterMap.remove(DISTINCT_PARAM), query, ctx);
		parseLimit(parameterMap.remove(LIMIT_PARAM), query, ctx);
		parseOffset(parameterMap.remove(OFFSET_PARAM), query, ctx);
		parseFilters(parameterMap, query, ctx);
		//TODO parse group, from, union
		return query;
	}
	
	default void parseViews(String[] parameters, QueryComposer composer, RequestContext ctx) { //ctes
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> evaluateView(e, ctx, true))
				.<Query>mapMulti((v,cons)-> {
					if(v instanceof Query q) {
						cons.accept(q);
					}
				}).forEach(composer::ctes);
		}
	}
	
	default void parseColumns(String[] parameters, QueryComposer composer, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			var def = ctx.getDialect().select(composer);
			def.invoke(ctx.resolveArgs(parse(parameters).toArray(Entry[]::new), null, def));
		}
		else {
			throw new IllegalArgumentException("missing required parameter: " + SELECT_OPR);
		}
	}
	
	default void parseOrders(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			var def = ctx.getDialect().order(query);
			def.invoke(ctx.resolveArgs(parse(parameters).toArray(Entry[]::new), null, def));
		}
	}
	
	default void parseJoins(String[] parameters, QueryComposer composer, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			var def = ctx.getDialect().join(composer);
			def.invoke(ctx.resolveArgs(parse(parameters).toArray(Entry[]::new), null, def));
		}
	}
	
	default void parseGroups(String[] parameters, QueryComposer composer, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			var def = ctx.getDialect().group(composer);
			def.invoke(ctx.resolveArgs(parse(parameters).toArray(Entry[]::new), null, def));
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
	
	default void parseDistinct(String[] parameters, QueryComposer composer, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			try {
				var def = ctx.getDialect().distinct(composer);
				def.invoke(ctx.resolveArgs(parse(parameters).toArray(Entry[]::new), null, def));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + DISTINCT_PARAM, e);
			}
		}
	}
	
	default void parseLimit(String[] parameters, QueryComposer composer, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			try {
				var limit = ctx.getDialect().limit(composer);
				limit.invoke(ctx.resolveArgs(parse(parameters).toArray(Entry[]::new), null, limit));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + LIMIT_PARAM, e);
			}
		}
	}
	
	default void parseOffset(String[] parameters, QueryComposer composer, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			try {
				var offset = ctx.getDialect().offset(composer);
				offset.invoke(ctx.resolveArgs(parse(parameters).toArray(Entry[]::new), null, offset));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + OFFSET_PARAM, e);
			}
		}
	}
	
	private static Stream<Entry> parse(String[] values) {
		return stream(values).flatMap(c-> stream(parseEntries(c)));
	}
	
	private static void resolveParameterCompatibility(Map<String, String[]> modifiableMap) {
		Map.of(COLUMN_PARAM, SELECT_OPR, FIELD_PARAM, SELECT_OPR).entrySet().forEach(e-> {
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
