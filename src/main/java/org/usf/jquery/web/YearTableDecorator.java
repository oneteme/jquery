package org.usf.jquery.web;

import static java.lang.String.join;
import static java.time.Month.DECEMBER;
import static java.time.YearMonth.now;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.PartitionedRequestQuery.monthFilter;
import static org.usf.jquery.core.PartitionedRequestQuery.yearColumn;
import static org.usf.jquery.core.PartitionedRequestQuery.yearTable;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.EMPTY_REVISION;
import static org.usf.jquery.web.Constants.REVISION;
import static org.usf.jquery.web.Constants.REVISION_MODE;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResouceException;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;
import static org.usf.jquery.web.ParseException.cannotParseException;
import static org.usf.jquery.web.TableDecorator.flatParameters;
import static org.usf.jquery.web.YearTableMetadata.yearTableMetadata;

import java.time.Year;
import java.time.YearMonth;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.usf.jquery.core.DBTable;
import org.usf.jquery.core.PartitionedRequestQuery;
import org.usf.jquery.core.RequestQuery;

/**
 * 
 * @author u$f
 * 
 * @see ColumnDecorator
 * @see YearTableMetadata
 *
 */
public interface YearTableDecorator extends TableDecorator {
	
	ColumnDecorator revisionColumn();

	/**
	 * loaded from db if null
	 * 
	 */
    default YearMonth[] availableRevisions() {//reduce data revision access
    	return metadata().getRevisions(); 
    }
    
	@Override
	default DBTable table() {
		return yearTable(tableName(), identity());
	}
	
	@Override
	default RequestQuery query(RequestQueryParam ant, Map<String, String[]> parameterMap) {
		var query = new PartitionedRequestQuery(parseRevisions(ant, parameterMap));
		parseWindow(ant, query, parameterMap);
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
		var arr = parameterMap.get(REVISION_MODE);
		if(nonNull(arr) && arr.length > 1) {
			throw cannotEvaluateException(REVISION, join(", ", arr)); //multiple values
		}
		var mod = revisionMode(isEmpty(arr) || isBlank(arr[0]) ? defaultRevisionMode() : arr[0]);
		var values = parameterMap.containsKey(REVISION) 
				? flatParameters(parameterMap.get(REVISION))
    			.map(this::parseYearMonth)
    			.toArray(YearMonth[]::new)
    			: new YearMonth[] {now()};
		var revs = mod.apply(values);
		if(isEmpty(revs)) {
			throw noSuchResouceException(REVISION, 
					Stream.of(values).map(YearMonth::toString).collect(joining(", "))); //require available revisions
		}
		return revs;
    }
    
    default UnaryOperator<YearMonth[]> revisionMode(String mode) {
    	switch(mode) {
		case "strict" 		: return this::strictRevisions;
		case "preceding"	: return this::precedingRevisions;
		case "succeeding"	: return this::succeedingRevisions;
    	default 			: throw cannotEvaluateException(REVISION_MODE, mode);
    	}
    }
    
    default YearMonth latestRevision() {
    	return null; //TODO
    }
	
	private YearMonth[] strictRevisions(YearMonth[] values) {
		var revs = availableRevisions();
		return isEmpty(revs) || isEmpty(values) 
				? EMPTY_REVISION 
				: Stream.of(values)
				.filter(v-> Stream.of(revs).anyMatch(v::equals))
				.toArray(YearMonth[]::new);
	}
	
	private YearMonth[] precedingRevisions(YearMonth[] values) {
		var revs = availableRevisions();
		if(isEmpty(revs)) {
			return EMPTY_REVISION;
		}
		if(isEmpty(values)) {
			return new YearMonth[] {revs[0]};
		}
		List<YearMonth> list = new LinkedList<>();
		for(var v : values) {
			for(int i=0; i<revs.length; i++) {
				if(revs[i].compareTo(v) <= 0) {
					list.add(revs[i]); 
					break;
				}
			}
		}
		return list.isEmpty() ? EMPTY_REVISION : list.toArray(YearMonth[]::new);
	}
	
	private YearMonth[] succeedingRevisions(YearMonth[] values) {
		var revs = availableRevisions();
		if(isEmpty(revs)) {
			return EMPTY_REVISION;
		}
		if(isEmpty(values)) {
			return new YearMonth[] {revs[0]};
		}
		List<YearMonth> list = new LinkedList<>();
		for(var v : values) {
			for(int i=revs.length-1; i>=0; i--) {
				if(revs[i].compareTo(v) >= 0) {
					list.add(revs[i]); 
					break;
				}
			}
		}
		return list.isEmpty() ? EMPTY_REVISION : list.toArray(YearMonth[]::new);
	}
	
    default YearMonth parseYearMonth(String revision) {
    	if(revision.matches("^\\d{4}$")) {
    		return Year.parse(revision).atMonth(DECEMBER);
    	}
    	try {
    		return YearMonth.parse(revision);
    	}
    	catch (Exception e) {
    		throw cannotParseException(REVISION, revision ,e);
		}
    }

    @Override
    default YearTableMetadata metadata() {
    	return (YearTableMetadata) TableDecorator.super.metadata();
    }
    
    @Override
    default YearTableMetadata createMetadata(Collection<ColumnDecorator> columns) {
    	var rc = ofNullable(revisionColumn()).flatMap(this::columnName).orElse(null);
    	return yearTableMetadata(this, rc, columns);
    }
    
    
    //TODO delegated not working
    default String defaultRevisionMode() {
    	return "strict";
    }
}
