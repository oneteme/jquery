package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static org.usf.jquery.web.TableDecorator.flatStream;

import java.time.Year;
import java.time.YearMonth;
import java.util.Map;

import org.usf.jquery.core.PartitionedRequestQuery;
import org.usf.jquery.core.RequestQuery;

public interface YearTableDecorator extends TableDecorator {
	
	ColumnDecorator revisionColumn();
	
	@Override
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var revs = revisionColumn() == null ? null: revisionColumn().column(this);
		var query = new PartitionedRequestQuery(revs, parseRevisions(ant, parameterMap));
		return query.select(this)
				.columns(parseColumns(ant, parameterMap))
				.filters(parseFilters(ant, parameterMap));
	}

	default YearMonth[] parseRevisions(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var values = parameterMap.get(ant.revisionParameter());
		var revs = values == null 
				? null
				: flatStream(values)
    			.map(YearTableDecorator::parseYearMonth)
    			.toArray(YearMonth[]::new);
		var meta = DatabaseScanner.get().metadata().table(this);
    	return meta.filterExistingRevision(ant.revisionMode(), revs);
    }

    static YearMonth parseYearMonth(String revision) {
    	if(revision.matches("[0-9]{4}-[0-9]{2}")) {
    		return YearMonth.parse(revision);
    	}
    	if(revision.matches("[0-9]{4}")) {
    		return Year.parse(revision).atMonth(DECEMBER);
    	}
    	throw new IllegalArgumentException("cannot parse revision " + revision);
    }

}
