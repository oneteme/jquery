package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.ArgumentParsers.parseBoolean;
import static org.usf.jquery.web.EntryChainParser.parseEntries;
import static org.usf.jquery.web.EntryChainParser.parseEntry;
import static org.usf.jquery.web.Parameters.COLUMN;
import static org.usf.jquery.web.Parameters.DISTINCT;
import static org.usf.jquery.web.Parameters.JOIN;
import static org.usf.jquery.web.Parameters.LIMIT;
import static org.usf.jquery.web.Parameters.OFFSET;
import static org.usf.jquery.web.Parameters.ORDER;
import static org.usf.jquery.web.Parameters.VIEW;

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
	
	public final QueryComposer parse(QueryContext context, Map<String, String[]> parameterMap) {
		try {
			parseViews(context, parameterMap.remove(VIEW));
			parseColumns(context, parameterMap.remove(COLUMN));
			parseOrders(context, parameterMap.remove(ORDER));
			parseJoins(context, parameterMap.remove(JOIN));
			parseLimit(context, parameterMap.remove(LIMIT));
			parseOffset(context, parameterMap.remove(OFFSET));
			parseDistinct(context, parameterMap.remove(DISTINCT));
			//parse iterator
			parseFilters(context, parameterMap); //remove all entries before parse filters
			return context.getMainQuery();
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
	}
	
	private static String formatException(Throwable e) {
		return e.getClass().getSimpleName() + ": " + e.getMessage();
	}

	protected void parseDistinct(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			context.getMainQuery().distinct(parseBoolean(requireNArgs(1, values, ()-> DISTINCT)[0]));
		}
	}
	
	protected void parseViews(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.map(e-> context.declareView(e.evalView(context)))
			.forEach(v->{ //!ViewDecorator
				if(v instanceof QueryDecorator qd) {
					context.getMainQuery().ctes(qd.getQuery());
				}
			});
		}
	}
	
	protected void parseColumns(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(v-> stream(parseEntries(v)))
			.map(e-> (NamedColumn)e.evalColumn(context, true))
			.forEach(context.getMainQuery()::columns);
		}
		else {
			throw new IllegalArgumentException("no columns specified");
		}
	}

	protected void parseOrders(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> context.getMainQuery().orders(e.evalOrder(context)));
		}
	}
	
	protected void parseJoins(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> context.getMainQuery().joins(e.evalJoin(context)));
		}
	}

	protected void parseLimit(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			context.getMainQuery().limit(requirePositiveInt(values, LIMIT));
		}
	}
	
	protected void parseOffset(QueryContext context, String[] values) {
		if(!isEmpty(values)) {
			context.getMainQuery().offset(requirePositiveInt(values, OFFSET));
		}
	}
	
	protected void parseFilters(QueryContext context, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.flatMap(e-> {
    		var ec = parseEntry(e.getKey());
    		return Stream.of(e.getValue()).map(v-> ec.evalFilter(context, parseEntries(v)));
    	})
    	.forEach(context.getMainQuery()::filters);
	}
	
	private static int requirePositiveInt(String[] values, String name) {
		var v = parseInt(requireNArgs(1, values, ()-> name)[0]);
		if(v >= 0) {
			return v;
		}
		throw new IllegalArgumentException(name + " parameter cannot be negative");
	}
}
