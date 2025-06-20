package org.usf.jquery.web;

import static java.lang.String.join;
import static java.time.Month.DECEMBER;
import static java.time.YearMonth.now;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.YearViewDecorator.YearMonths.groupByYear;

import java.time.Year;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Stream;

public final class RevisionRequestParser extends DefaultRequestParser {

	static final String REVISION = "revision";
	static final String REVISION_MODE = "revision.mode"; 
	
	@Override
	protected void parseFilters(QueryContext context, Map<String, String[]> parameters) {
		if(context.getDefaultView() instanceof YearViewDecorator vd) {
			parseRevisions(vd, parameters);
		}
		super.parseFilters(context, parameters);
	}
	
	void parseRevisions(YearViewDecorator vd, Map<String, String[]> parameterMap) {
		var arr = parameterMap.remove(REVISION_MODE);
		if(nonNull(arr) && arr.length > 1) {
			throw new IllegalArgumentException("too many " + REVISION_MODE + " " + join(", ", arr)); //multiple values
		}
		var mod = vd.revisionMode(isEmpty(arr) ? vd.defaultRevisionMode() : arr[0]);
		var values = parameterMap.containsKey(REVISION) 
				? flatParameters(parameterMap.remove(REVISION))
    			.map(RevisionRequestParser::parseYearMonth)
    			.toArray(YearMonth[]::new)
    			: new YearMonth[] {now()};
		var revs = mod.apply(values);
		if(!isEmpty(revs)) {
    		var query = currentEnvironment().currentQuery();
    		query.repeat(groupByYear(revs));
    		var filter = vd.monthFilter();
    		if(nonNull(filter)) {  //optional month revision
    			query.filters(filter);
    		}
		}
		else {
			throw noSuchResourceException(REVISION, 
					Stream.of(values).map(YearMonth::toString).collect(joining(", "))); //require available revisions
		}
    }
	
    static YearMonth parseYearMonth(String revision) {
    	if(revision.matches("^\\d{4}$")) {
    		return Year.parse(revision).atMonth(DECEMBER);
    	}
    	try {
    		return YearMonth.parse(revision);
    	}
    	catch (Exception e) {
    		throw new IllegalArgumentException("cannot parse revision " + revision, e);
		}
    }
    
	private static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}
}
