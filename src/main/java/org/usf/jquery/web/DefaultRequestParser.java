package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.ArgumentParsers.parseBoolean;
import static org.usf.jquery.web.EntryChainParser.parseEntries;
import static org.usf.jquery.web.EntryChainParser.parseEntry;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
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
	
	public final QueryComposer parse(ExecutionContext context, String defaultView, Map<String, String[]> parameterMap) {
		return context.query(q->{
			context.reset(context.lookupRegisteredView(defaultView)
					.orElseThrow(()-> noSuchResourceException(VIEW, defaultView)));
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
				return context.currentQuery();
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
	
	private static String formatException(Throwable e) {
		return e.getClass().getSimpleName() + ": " + e.getMessage();
	}

	protected void parseDistinct(ExecutionContext context, String[] values) {
		if(!isEmpty(values)) {
			context.currentQuery().distinct(parseBoolean(requireNArgs(1, values, ()-> DISTINCT)[0]));
		}
	}
	
	protected void parseViews(ExecutionContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.map(e-> context.declareView(e.evalView(context)))
			.forEach(v->{ //!ViewDecorator
				if(v instanceof QueryDecorator qd) {
					context.currentQuery().ctes(qd.getQuery());
				}
			});
		}
	}
	
	protected void parseColumns(ExecutionContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(v-> stream(parseEntries(v)))
			.map(e-> (NamedColumn)e.evalColumn(context, true))
			.forEach(context.currentQuery()::columns);
		}
		else {
			throw new IllegalArgumentException("no columns specified");
		}
	}

	protected void parseOrders(ExecutionContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> context.currentQuery().orders(e.evalOrder(context)));
		}
	}
	
	protected void parseJoins(ExecutionContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> context.currentQuery().joins(e.evalJoin(context)));
		}
	}

	protected void parseLimit(ExecutionContext context, String[] values) {
		if(!isEmpty(values)) {
			context.currentQuery().limit(requirePositiveInt(values, LIMIT));
		}
	}
	
	protected void parseOffset(ExecutionContext context, String[] values) {
		if(!isEmpty(values)) {
			context.currentQuery().offset(requirePositiveInt(values, OFFSET));
		}
	}
	
	protected void parseFilters(ExecutionContext context, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.flatMap(e-> {
    		var ec = parseEntry(e.getKey());
    		return Stream.of(e.getValue()).map(v-> ec.evalFilter(context, parseEntries(v)));
    	})
    	.forEach(context.currentQuery()::filters);
	}
	
	private static int requirePositiveInt(String[] values, String name) {
		var v = parseInt(requireNArgs(1, values, ()-> name)[0]);
		if(v >= 0) {
			return v;
		}
		throw new IllegalArgumentException(name + " parameter cannot be negative");
	}
}
