package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.PartitionedRequestQuery.monthFilter;
import static org.usf.jquery.core.PartitionedRequestQuery.yearColumn;
import static org.usf.jquery.core.PartitionedRequestQuery.yearTable;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.EMPTY_REVISION;
import static org.usf.jquery.web.Constants.REVISION;
import static org.usf.jquery.web.Constants.REVISION_MODE;
import static org.usf.jquery.web.RequestFilter.flatStream;

import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.usf.jquery.core.NamedTable;
import org.usf.jquery.core.PartitionedRequestQuery;
import org.usf.jquery.core.RequestQuery;

/**
 * 
 * @author u$f
 *
 */
public interface YearTableDecorator extends TableDecorator {
	
	ColumnDecorator revisionColumn();

    default YearMonth[] availableRevisions() {
    	return EMPTY_REVISION;
    }
	
	@Override
	default NamedTable table() {
		return yearTable(tableName()).as(reference());
	}
	
	@Override
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var query = new PartitionedRequestQuery(parseRevisions(ant, parameterMap)).select(table());
		parseColumns(ant, query, parameterMap);
		parseFilters(ant, query, parameterMap);
		parseOrders(ant, query, parameterMap);
		query.columns(yearColumn().as("revisionYear"));
		if(nonNull(revisionColumn())) { //optional revision column
			var col = revisionColumn().column(this);
			query.columns(col).filters(monthFilter(col));
		}
		return query;
	}

	default YearMonth[] parseRevisions(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var values = parameterMap.get(REVISION);
		var revs = isNull(values) 
				? new YearMonth[0]
				: flatStream(values)
    			.map(YearTableDecorator::parseYearMonth)
    			.toArray(YearMonth[]::new);
		return revisionMode(REVISION_MODE).apply(revs);
    }
    
    default UnaryOperator<YearMonth[]> revisionMode(String mode) {
    	if(isNull(mode)) {
    		return UnaryOperator.identity(); //no filter
    	}
    	switch (mode) {
		case "strict" : return this::strictRevisions;
		case "closest": return this::closestRevisions; //most recent, previous, next
    	default : throw new IllegalArgumentException("illegal revision mode " + mode);
    	}
    }
	
	private YearMonth[] strictRevisions(YearMonth[] values) {
		var revs = availableRevisions();
		return isEmpty(revs) || isEmpty(values) 
				? EMPTY_REVISION 
				: Stream.of(values)
				.filter(v-> Stream.of(revs).anyMatch(v::equals))
				.toArray(YearMonth[]::new);
	}
	
	private YearMonth[] closestRevisions(YearMonth[] values) {
		var revs = availableRevisions();
		if(isEmpty(revs)) {
			return EMPTY_REVISION;
		}
		if(isEmpty(values)) {
			return new YearMonth[] {revs[0]};
		}
		List<YearMonth> list = new LinkedList<>();
		for(var v : values) {
			Stream.of(revs)
			.filter(o-> o.compareTo(v) <= 0)
			.findFirst()
			.ifPresent(list::add);
		}
		return list.isEmpty() ? EMPTY_REVISION : list.toArray(YearMonth[]::new);
	}
	
    static YearMonth parseYearMonth(String revision) {
    	if(revision.matches("\\d{4}-\\d{2}")) {
    		return YearMonth.parse(revision);
    	}
    	if(revision.matches("\\d{4}")) {
    		return Year.parse(revision).atMonth(DECEMBER);
    	}
    	throw new IllegalArgumentException("cannot parse revision " + revision);
    }
    
}
