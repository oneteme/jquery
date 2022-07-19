package fr.enedis.teme.jquery.web;

import static fr.enedis.teme.jquery.web.ParameterInvalidValueException.invalidParameterValueException;
import static fr.enedis.teme.jquery.web.TableDescriptor.flatStream;
import static java.time.Month.DECEMBER;

import java.time.Year;
import java.time.YearMonth;
import java.util.Map;

import fr.enedis.teme.jquery.PartitionedRequestQuery;
import fr.enedis.teme.jquery.RequestQuery;

public interface YearTableDescriptor extends TableDescriptor {
	
	ColumnDescriptor revisionColumn();
	
	@Override
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var meta  = DatabaseScanner.get().metadata().table(this);
		var query = new PartitionedRequestQuery(revisionColumn().from(this), parseRevisions(ant, meta, parameterMap)) ;
		return query.select(value())
				.columns(ant.columns(), ()-> parseColumns(ant, meta, parameterMap))
				.filters(ant.filters(), ()-> parseFilters(ant, meta, parameterMap));
	}

	default YearMonth[] parseRevisions(RequestQueryParam ant, TableMetadata metadata, Map<String, String[]> parameterMap) {
		var values = parameterMap.get(ant.revisionParameter());
		var revs = values == null 
				? null
				: flatStream(values)
    			.map(YearTableDescriptor::parseYearMonth)
    			.toArray(YearMonth[]::new);
    	return metadata.filterExistingRevision(ant.revisionMode(), revs);
    }

    static YearMonth parseYearMonth(String rev) {
    	if(rev.matches("[0-9]{4}-[0-9]{2}")) {
    		return YearMonth.parse(rev);
    	}
    	if(rev.matches("[0-9]{4}")) {
    		return Year.parse(rev).atMonth(DECEMBER);
    	}
    	throw invalidParameterValueException(rev);
    }

}
