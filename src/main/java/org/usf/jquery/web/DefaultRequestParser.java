package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.EntryChainParser.parseEntries;
import static org.usf.jquery.web.EntryChainParser.parseEntry;
import static org.usf.jquery.web.Parameters.COLUMN;
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

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class DefaultRequestParser implements RequestParser {
	
	public final QueryComposer parse(RequestContext context, Map<String, String[]> parameterMap) {
		parseViews(context, parameterMap.remove(VIEW));
		parseColumns(context, parameterMap.remove(COLUMN));
		parseOrders(context, parameterMap.remove(ORDER));
		parseJoins(context, parameterMap.remove(JOIN));
		parseLimit(context, parameterMap.remove(LIMIT));
		parseOffset(context, parameterMap.remove(OFFSET));
		//parse iterator
		parseFilters(context, parameterMap); //remove all entries before parse filters
		return context.getQuery();
	}
	
	protected void parseViews(RequestContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.map(e-> currentContext().declareView(e.evalView(context)))
			.forEach(v->{ //!ViewDecorator
				if(v instanceof QueryDecorator qd) {
					context.getQuery().ctes(qd.getQuery());
				}
			});
		}
	}
	
	protected void parseColumns(RequestContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(v-> stream(parseEntries(v)))
			.map(e-> (NamedColumn)e.evalColumn(context, true))
			.forEach(context.getQuery()::columns);
		}
		else {
			throw new IllegalArgumentException("no columns specified");
		}
	}

	protected void parseOrders(RequestContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> context.getQuery().orders(e.evalOrder(context)));
		}
	}
	
	protected void parseJoins(RequestContext context, String[] values) {
		if(!isEmpty(values)) {
			Stream.of(values)
			.flatMap(c-> stream(parseEntries(c)))
			.forEach(e-> context.getQuery().joins(e.evalJoin(context)));
		}
	}

	protected void parseLimit(RequestContext context, String[] values) {
		if(!isEmpty(values)) {
			context.getQuery().limit(requirePositiveInt(values, LIMIT));
		}
	}
	
	protected void parseOffset(RequestContext context, String[] values) {
		if(!isEmpty(values)) {
			context.getQuery().offset(requirePositiveInt(values, OFFSET));
		}
	}
	
	protected void parseFilters(RequestContext context, Map<String, String[]> parameters) {
    	parameters.entrySet().stream()
    	.flatMap(e-> {
    		var ec = parseEntry(e.getKey());
    		return Stream.of(e.getValue()).map(v-> ec.evalFilter(context, parseEntries(v)));
    	})
    	.forEach(context.getQuery()::filters);
	}
	
	private static int requirePositiveInt(String[] values, String name) {
		var v = parseInt(requireNArgs(1, values, ()-> name)[0]);
		if(v >= 0) {
			return v;
		}
		throw new IllegalArgumentException(name + " parameter cannot be negative");
	}
}
