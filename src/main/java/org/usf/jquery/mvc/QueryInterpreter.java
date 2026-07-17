package org.usf.jquery.mvc;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.concat;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.mvc.EntryEvaluators.evaluateCriteria;
import static org.usf.jquery.mvc.EntryParser.parseEntries;
import static org.usf.jquery.mvc.EntryParser.parseEntry;
import static org.usf.jquery.mvc.Parameters.CTE_PARAM;
import static org.usf.jquery.mvc.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.mvc.Parameters.GROUP_PARAM;
import static org.usf.jquery.mvc.Parameters.JOIN_PARAM;
import static org.usf.jquery.mvc.Parameters.LIMIT_PARAM;
import static org.usf.jquery.mvc.Parameters.OFFSET_PARAM;
import static org.usf.jquery.mvc.Parameters.ORDER_PARAM;
import static org.usf.jquery.mvc.Parameters.SELECT_PARAM;
import static org.usf.jquery.mvc.Parameters.UNION_PARAM;
import static org.usf.jquery.mvc.Parameters.VIEW_PARAM;
import static org.usf.jquery.mvc.QueryExtension.Modifier.MERGE;
import static org.usf.jquery.mvc.QueryExtension.Modifier.REJECT;
import static org.usf.jquery.mvc.QueryExtension.Modifier.REPLACE;
import static org.usf.jquery.mvc.RestrictedStore.restrict;
import static org.usf.jquery.mvc.StoreManager.getInstance;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.usf.jquery.core.ComposerDefinition;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.mvc.QueryExtension.Modifier;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 */
@Slf4j
public class QueryInterpreter {
	
	static final Set<String> NATIVE_PARAMETERS = Set.of(
			CTE_PARAM, SELECT_PARAM, JOIN_PARAM, ORDER_PARAM, 
			DISTINCT_PARAM, LIMIT_PARAM, OFFSET_PARAM, VIEW_PARAM);
	
	public MvcRequest resolveQueryComposer(@NonNull Method method, @NonNull Map<String, String[]> parameterMap) {
		var tmp = method.getAnnotation(QueryTemplate.class);
		if(nonNull(tmp)) {
			var store = tmp.store() == StoreCatalog.class //default value
					? getInstance().getDefaultStore() 
					: getInstance().getStore(tmp.store());
			var guard = method.getAnnotation(QueryGuard.class);
			var secStore = nonNull(guard) 
					? restrict(store, guard.maxCols(), guard.maxRows(), guard.aggregate(), 
							Set.of(guard.excludeResources()), Set.of(guard.excludeDialects()))
					: store;
			var map = new LinkedHashMap<>(parameterMap); //modifiable map + preserve order
			if(!isEmpty(tmp.ignore())) {
				for(var k : tmp.ignore()) {
					if(nonNull(map.remove(k))) {
						log.debug("ignoring parameter '{}'", k);
					}
				}
			}
			resolveParameterCompatibility(map);
			mergeParameters(tmp, method.getAnnotation(QueryExtension.class), map);
			var viewer = parseViewer(secStore, VIEW_PARAM, parameterMap); //TD accept alias for download
			var composer = parseQuery(secStore, tmp.dataset(), map);
			return new MvcRequest(secStore, composer, viewer);
		}
		throw new IllegalStateException("");
	}
	
	QueryComposer parseQuery(StoreCatalog store, String dataset, Map<String, String[]> parameterMap) {
		var ctx = store.createContext(dataset);
		var query = new QueryComposer();
		parseParam(parameterMap, CTE_PARAM, ctx.getDialect().cte(query), ctx);
		parseParam(parameterMap, JOIN_PARAM, ctx.getDialect().join(query), ctx);
		parseParam(parameterMap, SELECT_PARAM, ctx.getDialect().select(query), ctx);
		parseParam(parameterMap, GROUP_PARAM, ctx.getDialect().group(query), ctx);
		parseParam(parameterMap, ORDER_PARAM, ctx.getDialect().order(query), ctx);
		parseParam(parameterMap, DISTINCT_PARAM, ctx.getDialect().distinct(query), ctx);
		parseParam(parameterMap, LIMIT_PARAM, ctx.getDialect().limit(query), ctx);
		parseParam(parameterMap, OFFSET_PARAM, ctx.getDialect().offset(query), ctx);
		parseParam(parameterMap, UNION_PARAM, ctx.getDialect().union(query), ctx);
		//TD parse group, from, union
		parseFilters(parameterMap, ctx.getDialect().criteria(query), ctx);
		return query;
	}
	
	ResultSetViewer parseViewer(StoreCatalog store, String param, @NonNull Map<String, String[]> parameterMap){
		var params = parameterMap.remove(param);
		if(!isEmpty(params)) {
			if(params.length == 1) {
				var view = store.viewRegistry().geViewer(params[0]);
				if(nonNull(view)) {
					return view;
				}
				throw new IllegalArgumentException("unknown view: " + params[0]);
			}
			throw new IllegalArgumentException("parameter '%s' can only have one value".formatted(param));
		}
		throw new IllegalArgumentException("require '%s' parameter".formatted(param));
	}
	
	void parseParam(Map<String, String[]> parameterMap, String param, ComposerDefinition<QueryComposer> def, RequestContext ctx) { //ctes
		var params = parameterMap.remove(param);
		if(nonNull(params)) {
			try {
				def.invoke(ctx.resolveArgs(parse(params).toArray(Entry[]::new), null, def));
			}
			catch (Exception e) {
				throw new IllegalArgumentException("invalid value for parameter: " + param, e);
			}
		}
	}
	
	void parseFilters(Map<String, String[]> parameterMap, ComposerDefinition<QueryComposer> def, RequestContext ctx) {
		if(!isEmpty(parameterMap)) {
			Object[] args = parameterMap.entrySet().stream()
					.map(e-> evaluateCriteria(parseEntry(e.getKey()), ctx, parse(e.getValue()).toArray(Entry[]::new)))
					.toArray(Criteria[]::new);
			def.invoke(args);
		}
	}
	
	static void mergeParameters(QueryTemplate template, QueryExtension extention, Map<String, String[]> parameters) {
		if(nonNull(extention)) {
			resolveParameterValues(CTE_PARAM, extention.cte(), parameters, template.cte());
			resolveParameterValues(SELECT_PARAM, extention.select(), parameters, template.select());
			resolveParameterValues(JOIN_PARAM, extention.join(), parameters, template.join());
			resolveParameterValues(ORDER_PARAM, extention.order(), parameters, template.order());
			resolveParameterValues(DISTINCT_PARAM, extention.overrideDistinct(), parameters, template.distinct(), v-> v);
			resolveParameterValues(LIMIT_PARAM, extention.overrideLimit(), parameters, template.limit(), v-> v > 0);
			resolveParameterValues(OFFSET_PARAM, extention.overrideOffset(), parameters, template.offset(), v-> v > 0);
			resolveParameterValues(VIEW_PARAM, extention.overrideView(), parameters, template.view(), s-> !s.isEmpty());
			if(!extention.acceptCriteria()) {
				for(var v : parameters.keySet()) {
					if(NATIVE_PARAMETERS.contains(v)) {
						throw new IllegalArgumentException("parameter '" + v + "' is not allowed");
					}
				}
			}
		}
		else {
			resolveParameterValues(CTE_PARAM, REPLACE, parameters, template.cte());
			resolveParameterValues(SELECT_PARAM, REPLACE, parameters, template.select());
			resolveParameterValues(JOIN_PARAM, REPLACE, parameters, template.join());
			resolveParameterValues(ORDER_PARAM, REPLACE, parameters, template.order());
			resolveParameterValues(DISTINCT_PARAM, true, parameters, template.distinct(), v-> v);
			resolveParameterValues(LIMIT_PARAM, true, parameters, template.limit(), v-> v > 0);
			resolveParameterValues(OFFSET_PARAM, true, parameters, template.offset(), v-> v > 0);
			resolveParameterValues(VIEW_PARAM, true, parameters, template.view(), s-> !s.isEmpty());
		}
	}
	
	static void resolveParameterValues(String key, Modifier modifier, Map<String, String[]> parameters, String[] values) {
		var arr = parameters.get(key); 
		if(modifier == MERGE) {
			if(nonNull(arr) && values.length > 0) {
				arr = concat(stream(values), stream(arr)).toArray(String[]::new);
			}
			else if(values.length > 0) {
				arr = values;
			}
		}
		else if(modifier == REPLACE) {
			if(isNull(arr) && values.length > 0) {
				arr = values;
			}
		}
		else if(modifier == REJECT)  { // REJECT 
			if(nonNull(arr)) {
				throw new IllegalArgumentException("parameter '" + key + "' is already defined and cannot be overridden");
			}
		}
		else {
			throw new UnsupportedOperationException("unsupported modifier: " + modifier);
		}
		if(nonNull(arr)) {
			parameters.put(key, arr);
		}
	}

	static  <T> void resolveParameterValues(String key, boolean override, Map<String, String[]> parameters, T value, Predicate<T> pr) {
		var arr = parameters.get(key);
		if(!override && nonNull(arr)) {
			throw new IllegalArgumentException("parameter '" + key + "' is already defined and cannot be overridden");
		}
		if(isNull(arr) && pr.test(value)) {
			log.debug("setting parameter '{}' with value {}", key, value);
			arr = new String[] { String.valueOf(value) };
		}
		if(nonNull(arr)) {
			parameters.put(key, arr);
		}
	}

	@Deprecated(forRemoval = true, since = "5.0.0")
	private static void resolveParameterCompatibility(Map<String, String[]> modifiableMap) {
		Map.of("column", SELECT_PARAM).entrySet().forEach(e-> {
			var args = modifiableMap.remove(e.getKey());
			if(!isEmpty(args)) {
				log.warn("'{}' parameter is deprecated, use {} instead", e.getKey(), e.getValue());
				modifiableMap.compute(e.getValue(), (k, v)-> isEmpty(v) 
						? args 
						: concat(stream(v), stream(args)).toArray(String[]::new));
			}
		});
	}
	
	private static Stream<Entry> parse(String[] values) {
		return stream(values).flatMap(c-> stream(parseEntries(c)));
	}
}
