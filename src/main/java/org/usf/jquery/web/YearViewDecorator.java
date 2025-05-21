package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static java.time.YearMonth.now;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.DBColumn.constant;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.YearViewDecorator.YearMonths.from;

import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.TableView;

/**
 * 
 * @author u$f
 * 
 */
//@Deprecated(since = "4.0.0", forRemoval = true)
public interface YearViewDecorator extends ViewDecorator {

	static final String REVISION = "revision";
	static final String REVISION_MODE = "revision.mode";
	static final YearMonth[] EMPTY_REVISION = new YearMonth[0];
	
	ColumnDecorator monthRevision(); //optional
	
	@Override
	default DBView view() {
		var env = currentEnvironment();
		return env.cacheView(identity(), ()-> {
			var view = env.getDatabase().view(this);
			if(view instanceof TableView t) {
				return t.withAdjuster((m, v)-> v + "_" + ((YearMonths)m).year());
			}
			throw new UnsupportedOperationException(requireNonNull(view).getClass().getSimpleName());
		});
	}
	
	@Override
	default Builder<DBFilter> criteria(String name) {
		return REVISION.equals(name) 
				? revisionFilter()
				: ViewDecorator.super.criteria(name);
	}
	
	@Override
	default ViewMetadata metadata(Map<String, ColumnMetadata> colMetadata) {
		var mc = monthRevision();
		return new YearTableMetadata(this, nonNull(mc) ? columnName(mc) : null, colMetadata);
	}

	default Builder<DBFilter> revisionFilter() { //optional filter ! monthRevision
    	return (view, env, args)-> {
    		var arr = isEmpty(args)
    				? new YearMonth[] {now()}
    				: flatParameters(args).map(YearViewDecorator::parseYearMonth).toArray(YearMonth[]::new);
    		var query = env.currentQuery();
    		query.repeat(from(revisionMode(query, arr)));
    		return nonNull(monthRevision()) //optional month column
    				? view.column(monthRevision()).filter(in().expression((m,v)-> 
    					((YearMonths) requireNonNull(m, REVISION)).months()))
    				: constant(1).eq(1); //avoid returning null
    	};
    }
	
	static Builder<DBColumn> yearRevision() {
		return (view, env, args)-> {
			if(view instanceof YearViewDecorator) {
				return constant(INTEGER, (m, v)-> ((YearMonths)m).year());
			}
			throw new IllegalStateException(view.getClass().getSimpleName() + " is not a YearView");
		};
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
    
    final record YearMonths(int year, Integer[] months) {
    	
    	static YearMonths[] from(YearMonth[] arr) {
    		return stream(arr).collect(groupingBy(YearMonth::getYear)).entrySet()
    				.stream().map(e-> new YearMonths(e.getKey(), e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)))
    				.toArray(YearMonths[]::new);
    	}
    }
    
    //revision.mode

    @Deprecated(since = "v4", forRemoval = true)
    default YearMonth[] revisionMode(QueryComposer query, YearMonth[] arr) {
    	var args = query.getVariables(REVISION_MODE);
    	if(isEmpty(args)) {
    		args = new String[] {defaultRevisionMode()};
    	}
    	else if(args.length > 1) {
    		throw new IllegalArgumentException("too many " + REVISION_MODE + " " + String.join(", ", args)); //multiple values
    	}
    	var revs = switch(args[0]) {
		case "strict" 		-> strictRevisions(arr);
		case "preceding"	-> precedingRevisions(arr);
		case "succeeding"	-> succeedingRevisions(arr);
    	default 			-> throw new IllegalArgumentException("cannot parse revision mode " + args[0]);
    	};
		if(!isEmpty(revs)) {
			return revs;
		}
		throw noSuchResourceException(REVISION, 
				Stream.of(revs).map(YearMonth::toString).collect(joining(", "))); //require available revisions
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
		List<YearMonth> list = new ArrayList<>();
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
		List<YearMonth> list = new ArrayList<>();
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

    default YearMonth[] availableRevisions() {//cache
    	var meta = currentEnvironment().getMetadata().viewMetadata(this);
    	return ((YearTableMetadata)meta).getRevisions(); 
    }

    default String defaultRevisionMode() {
    	return "strict";
    }
    
	private static Stream<String> flatParameters(String... arr) { //number local separator
		return Stream.of(arr).flatMap(v-> Stream.of(v.split(",")));
	}	
}
