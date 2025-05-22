package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.ArgumentParsers.parseBoolean;
import static org.usf.jquery.web.EntryChainParser.parseEntries;
import static org.usf.jquery.web.EntryChainParser.parseEntry;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.VIEW_PARAM;

import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.QueryComposer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRequestParser implements RequestParser {
	
	public final QueryComposer parse(Environment env, String defaultView, String[] variables, Map<String, String[]> parameterMap) {
		var ctx = new QueryContext(ofNullable(env.getViews().get(defaultView))
				.orElseThrow(()-> noSuchResourceException(VIEW_PARAM, defaultView)));
		return env.query(q->{
			try {
				if(!isEmpty(variables)) {
					for(var v : variables) {
						q.variable(v, parameterMap.remove(v));
					}
				}
				parseViews(ctx, parameterMap.remove(VIEW_PARAM));
				parseColumns(ctx, parameterMap.remove(COLUMN_PARAM));
				parseDistinct(ctx, parameterMap.remove(DISTINCT_PARAM));
				parseOrders(ctx, parameterMap.remove(ORDER_PARAM));
				parseJoins(ctx, parameterMap.remove(JOIN_PARAM));
				parseLimit(ctx, parameterMap.remove(LIMIT_PARAM));
				parseOffset(ctx, parameterMap.remove(OFFSET_PARAM));
				parseFilters(ctx, parameterMap); //remove all entries before parse filters
			} catch (WebException e) {
				if(log.isTraceEnabled()) {
					log.trace(formatException(e));
					var shift = 0;
					Throwable ex = e.getCause();
					while(nonNull(ex)){
						log.trace("  ".repeat(++shift) + "~> " + formatException(ex));
						ex = ex.getCause();
					}
				}
				throw e;
			}
		});
	}

	protected void parseDistinct(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			currentEnvironment().currentQuery().distinct(parseBoolean(requireNArgs(1, values, ()-> DISTINCT_PARAM)[0]));
		}
	}
	
	protected void parseViews(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.map(e-> context.declareView(e.evalView(context)))
			.forEach(v->{ //!ViewDecorator
				if(v instanceof QueryDecorator qd) {
					currentEnvironment().currentQuery().ctes(qd.getQuery());
				}
			});
		}
	}
	
	protected void parseColumns(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(v-> stream(parseEntries(v)))
			.map(e-> (NamedColumn)e.evalColumn(context, true))
			.forEach(currentEnvironment().currentQuery()::columns);
		}
		else {
			throw new IllegalArgumentException("no columns specified");
		}
	}

	protected void parseOrders(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> currentEnvironment().currentQuery().orders(e.evalOrder(context)));
		}
	}
	
	protected void parseJoins(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> currentEnvironment().currentQuery().joins(e.evalJoin(context)));
		}
	}

	protected void parseLimit(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			currentEnvironment().currentQuery().limit(requirePositiveInt(values, LIMIT_PARAM));
		}
	}
	
	protected void parseOffset(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			currentEnvironment().currentQuery().offset(requirePositiveInt(values, OFFSET_PARAM));
		}
	}
	
	protected void parseFilters(QueryContext context, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.flatMap(e-> {
    		var ec = parseEntry(e.getKey());
    		return Stream.of(e.getValue()).map(v-> ec.evalFilter(context, parseEntries(v)));
    	})
    	.forEach(currentEnvironment().currentQuery()::filters);
	}
	
	private static int requirePositiveInt(String[] values, String name) {
		var v = parseInt(requireNArgs(1, values, ()-> name)[0]);
		if(v >= 0) {
			return v;
		}
		throw new IllegalArgumentException(name + " parameter cannot be negative");
	}
	
	private static String formatException(Throwable e) {
		return e.getClass().getSimpleName() + ": " + e.getMessage();
	}
}
