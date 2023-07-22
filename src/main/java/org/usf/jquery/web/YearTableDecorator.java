package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static org.usf.jquery.core.PartitionedRequestQuery.monthFilter;
import static org.usf.jquery.core.PartitionedRequestQuery.yearColumn;
import static org.usf.jquery.core.PartitionedRequestQuery.yearTable;
import static org.usf.jquery.web.RequestFilter.flatStream;

import java.time.Year;
import java.time.YearMonth;
import java.util.Map;

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
	
	@Override
	default NamedTable table() {
		return yearTable(tableName()).as(reference());
	}
	
	@Override
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var query = new PartitionedRequestQuery(parseRevisions(ant, parameterMap)).select(table());
		parseColumns(ant, query, parameterMap);
		parseFilters(ant, query, parameterMap);
		query.columns(yearColumn().as("revisionYear"));
		if(revisionColumn() != null) { //optional revision column
			var rev = revisionColumn().column(this);
			query.columns(rev);
			query.filters(monthFilter(rev));
		}
		return query;
	}

	default YearMonth[] parseRevisions(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var values = parameterMap.get(ant.revisionParameter());
		var revs = values == null 
				? null
				: flatStream(values)
    			.map(YearTableDecorator::parseYearMonth)
    			.toArray(YearMonth[]::new);
    	return metadata().filterExistingRevision(ant.revisionMode(), revs);
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
    
    @Override
    default YearTableMetadata metadata() {
    	return (YearTableMetadata) TableDecorator.super.metadata();
    }
    
}
