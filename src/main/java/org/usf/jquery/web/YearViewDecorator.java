package org.usf.jquery.web;

import static java.lang.String.join;
import static java.time.Month.DECEMBER;
import static java.time.YearMonth.now;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Constants.EMPTY_REVISION;
import static org.usf.jquery.web.Constants.REVISION;
import static org.usf.jquery.web.Constants.REVISION_MODE;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.JQueryContext.database;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResouceException;
import static org.usf.jquery.web.EntryParseException.cannotParseEntryException;
import static org.usf.jquery.web.RevisionIterator.iterator;
import static org.usf.jquery.web.RevisionIterator.monthFilter;
import static org.usf.jquery.web.RevisionIterator.yearColumn;
import static org.usf.jquery.web.RevisionIterator.yearTable;
import static org.usf.jquery.web.ViewDecorator.flatParameters;

import java.time.Year;
import java.time.YearMonth;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TableView;
import org.usf.jquery.core.TaggableColumn;

/**
 * 
 * @author u$f
 * 
 */
@Deprecated
public interface YearViewDecorator extends ViewDecorator {

	ColumnDecorator yearRevision(); //!table column
	
	ColumnDecorator monthRevision(); //optional

	/**
	 * loaded from db if null
	 * 
	 */
    default YearMonth[] availableRevisions() {//cache
    	return metadata().getRevisions(); 
    }
    
	@Override
	default TableView view() {
		return yearTable(viewName(), identity());
	}
	
	@Override
	default RequestQueryBuilder query(Map<String, String[]> parameterMap) {
		var query = ViewDecorator.super.query(parameterMap);
		monthRevision().map(this::column)
		.ifPresent(c-> query.filters(monthFilter(c)));
		return query.repeat(iterator(parseRevisions(parameterMap)));
	}

	default YearMonth[] parseRevisions(Map<String, String[]> parameterMap) {
		var arr = parameterMap.get(REVISION_MODE);
		if(nonNull(arr) && arr.length > 1) {
			throw EvalException.cannotEvaluateException(REVISION, join(", ", arr)); //multiple values
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
	
	@Override
	default TaggableColumn column(ColumnDecorator column) {
		return column == yearRevision() 
				? yearColumn().as(yearRevision().reference(this)) 
				: ViewDecorator.super.column(column);
	}
    
    default UnaryOperator<YearMonth[]> revisionMode(String mode) {
    	switch(mode) {
		case "strict" 		: return this::strictRevisions;
		case "preceding"	: return this::precedingRevisions;
		case "succeeding"	: return this::succeedingRevisions;
    	default 			: throw EvalException.cannotEvaluateException(REVISION_MODE, mode);
    	}
    }
    
	default YearMonth[] strictRevisions(YearMonth[] values) {
		var revs = availableRevisions();
		return isEmpty(revs) || isEmpty(values) 
				? EMPTY_REVISION 
				: Stream.of(values)
				.filter(v-> Stream.of(revs).anyMatch(v::equals))
				.toArray(YearMonth[]::new);
	}
	
	default YearMonth[] precedingRevisions(YearMonth[] values) {
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
	
	default YearMonth[] succeedingRevisions(YearMonth[] values) {
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
    		throw cannotParseEntryException(REVISION, revision ,e);
		}
    }

    @Override
    default YearTableMetadata metadata() {
		return (YearTableMetadata) currentContext().computeTableMetadata(this, null); //safe cast
    }
    
    default String defaultRevisionMode() {
    	return "strict";
    }

}
