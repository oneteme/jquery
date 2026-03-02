package org.usf.jquery.web.proxy;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;
import static org.usf.jquery.web.proxy.EntryEvaluators.evaluateFilter;
import static org.usf.jquery.web.proxy.EntryParser.parseEntries;
import static org.usf.jquery.web.proxy.EntryParser.parseEntry;
import static org.usf.jquery.web.proxy.JQueryManager.getDefaultSchema;
import static org.usf.jquery.web.proxy.JQueryManager.getSchema;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.web.ResourceAccessException;

import lombok.NonNull;

public interface RequestQueryMapper {
	
	default QueryComposer requestQuery(@NonNull QueryRequest qr, @NonNull Map<String, String[]> parameterMap) {
		var modifiableMap = new LinkedHashMap<>(parameterMap); //modifiable map + preserve order
		if(!isEmpty(qr.ignoreParameters())) {
			for(var k : qr.ignoreParameters()) {
				if(modifiableMap.containsKey(k)) {
					modifiableMap.remove(k);
				}
			}
		}
		modifiableMap.computeIfAbsent(COLUMN_PARAM, k-> qr.defaultColumns());
		var schema = qr.database() == SchemaResource.class 
				? getDefaultSchema() 
				: getSchema(qr.database());
		var query= parse(modifiableMap, schema.createContext(qr.view()));
		if(!qr.aggregationOnly() || query.isAggregation()) {
			return query; 
		}
		throw new ResourceAccessException("query is not aggregation");
	}
	
	private QueryComposer parse(Map<String, String[]> parameterMap, RequestContext ctx) {
		var query = new QueryComposer();
		parseViews(parameterMap.remove(VIEW_PARAM), query, ctx);
		parseColumns(parameterMap.remove(COLUMN_PARAM), query, ctx);
		parseJoins(parameterMap.remove(JOIN_PARAM), query, ctx);
		parseOrders(parameterMap.remove(ORDER_PARAM), query, ctx);
		parseDistinct(parameterMap.remove(DISTINCT_PARAM), query, ctx);
		parseLimit(parameterMap.remove(LIMIT_PARAM), query, ctx);
		parseOffset(parameterMap.remove(OFFSET_PARAM), query, ctx);
		parseFilters(parameterMap, query, ctx);
		return query;
	}
	
	private void parseViews(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, DBView.class))
				.<QueryView>mapMulti((v,cons)-> {
					if(v instanceof QueryView q) {
						cons.accept(q);
					}
				}).forEach(query::ctes);
		}
	}
	
	private void parseColumns(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, NamedColumn.class)).forEach(query::columns);
		}
		else {
			throw new IllegalArgumentException("Missing required parameter: " + COLUMN_PARAM);
		}
	}
	
	private void parseOrders(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, DBOrder.class)).forEach(query::orders);
		}
	}
	
	private void parseJoins(String[] parameters, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameters)) {
			parse(parameters).map(e-> ctx.resolve(e, JoinsClause.class)).forEach(query::joins2);
		}
	}
	
	private void parseFilters(Map<String, String[]> parameterMap, QueryComposer query, RequestContext ctx) {
		if(!isEmpty(parameterMap)) {
			parameterMap.entrySet().forEach(e-> 
				query.filters(evaluateFilter(parseEntry(e.getKey()), ctx, parse(e.getValue()).toArray(Entry[]::new))));
		}
	}
	
	private void parseDistinct(String[] parameters, QueryComposer query, RequestContext ctx) {
		var parameter = optionalSingleParameter(parameters, DISTINCT_PARAM);
		if(nonNull(parameter) && !parameter.isEmpty()) {
			try {
				query.distinct(ctx.evalValue(parameter, Boolean.class));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + DISTINCT_PARAM, e);
			}
		}
	}
	
	private void parseLimit(String[] parameters, QueryComposer query, RequestContext ctx) {
		var parameter = optionalSingleParameter(parameters, LIMIT_PARAM);
		if(nonNull(parameter) && !parameter.isEmpty()) {
			try {
				query.limit(ctx.evalValue(parameter, Integer.class));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + LIMIT_PARAM, e);
			}
		}
	}
	
	private void parseOffset(String[] parameters, QueryComposer query, RequestContext ctx) {
		var parameter = optionalSingleParameter(parameters, OFFSET_PARAM);
		if(nonNull(parameter) && !parameter.isEmpty()) {
			try {
				query.offset(ctx.evalValue(parameter, Integer.class));	
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Invalid value for parameter: " + OFFSET_PARAM, e);
			}
		}
	}
	
	private String optionalSingleParameter(String[] param, String paramName) {
		if(nonNull(param)) {
			if(param.length==1) {
				return param[0];
			}
			if(param.length > 1) {
				throw new IllegalArgumentException("Multiple values provided for parameter: " + paramName);
			}
		}
		return null;
	}
	
	private static Stream<Entry> parse(String[] values) {
		return stream(values).flatMap(c-> stream(parseEntries(c)));
	}
}
