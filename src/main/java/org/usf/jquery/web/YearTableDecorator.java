package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static org.usf.jquery.web.TableDecorator.flatStream;

import java.time.Year;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

import org.usf.jquery.core.PartitionedRequestQuery;
import org.usf.jquery.core.RequestQuery;

public interface YearTableDecorator extends TableDecorator {
	
	ColumnDecorator revisionColumn();
	
	@Override
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var meta = DatabaseScanner.get().metadata().table(this);
		var revs = revisionColumn() == null ? null: revisionColumn();
		var query = new PartitionedRequestQuery(revs, parseRevisions(ant, meta, parameterMap));
		var columns = parseColumns(ant, parameterMap);
		var filters = parseFilters(ant, Set.of(columns), parameterMap);
		return query.select(this)
				.columns(columns)
				.filters(filters);
	}

	default YearMonth[] parseRevisions(RequestQueryParam ant, TableMetadata metadata, Map<String, String[]> parameterMap) {
		var values = parameterMap.get(ant.revisionParameter());
		var revs = values == null 
				? null
				: flatStream(values)
    			.map(YearTableDecorator::parseYearMonth)
    			.toArray(YearMonth[]::new);
    	return metadata.filterExistingRevision(ant.revisionMode(), revs);
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
