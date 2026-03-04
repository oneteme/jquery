package org.usf.jquery.web;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.usf.jquery.core.Column.constant;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.currentEnvironment;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.TableView;

/**
 * 
 * @author u$f
 * 
 */
//@Deprecated(since = "4.0.0", forRemoval = true)
public interface YearViewDecorator extends ViewDecorator {

	static final YearMonth[] EMPTY_REVISION = new YearMonth[0];
	
	ColumnDecorator monthRevision(); //optional
	
	@Override
	default DBView view() {
		var env = currentEnvironment();
		return env.cacheView(identity(), ()-> {
			var view = env.getDatabase().view(this);
			if(view instanceof TableView t) {
				return t.withAdjuster((m, v)-> nonNull(m) ? v + "_" + ((YearMonths)m).year() : v); //avoid NullPointerEx on toString call
			}
			throw new UnsupportedOperationException(requireNonNull(view).getClass().getSimpleName());
		});
	}
	
	default Criteria monthFilter() {
		var mc = monthRevision();
		return nonNull(mc) 
				? column(mc).filter(in()
						.expression((m,v)-> ((YearMonths) requireNonNull(m, "revision")).months()))
				: null;
	}

	static Builder<ViewDecorator, Column> yearRevision() {
		return (view, args)-> {
			if(view instanceof YearViewDecorator) {
				return constant(INTEGER, (m, v)-> nonNull(m) ? ((YearMonths)m).year() : null);
			}
			throw new IllegalStateException(view.getClass().getSimpleName() + " is not a YearView");
		};
	}
	
    default UnaryOperator<YearMonth[]> revisionMode(String mode) {
    	switch(mode) {
		case "strict" 		: return this::strictRevisions;
		case "preceding"	: return this::precedingRevisions;
		case "succeeding"	: return this::succeedingRevisions;
    	default 			: throw new IllegalArgumentException("cannot parse revision mode " + mode);
    	}
    }
    
	default YearMonth[] strictRevisions(YearMonth[] values) {
		var revs = metadata().getRevisions();
		return isEmpty(revs) || isEmpty(values) 
				? EMPTY_REVISION 
				: Stream.of(values)
				.filter(v-> Stream.of(revs).anyMatch(v::equals))
				.toArray(YearMonth[]::new);
	}
	
	default YearMonth[] precedingRevisions(YearMonth[] values) {
		var revs = metadata().getRevisions();
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
		var revs = metadata().getRevisions();
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

    default String defaultRevisionMode() {
    	return "strict";
    }
	
	@Override
	default YearTableMetadata metadata(Map<String, ColumnMetadata> colMetadata) {
		var mc = monthRevision();
		return new YearTableMetadata(this, nonNull(mc) ? columnName(mc) : null, colMetadata);
	}
	
	@Override
	default YearTableMetadata metadata() {
		return (YearTableMetadata) ViewDecorator.super.metadata();
	}
	
    final record YearMonths(int year, Integer[] months) {
    	
    	static YearMonths[] groupByYear(YearMonth[] arr) {
    		return stream(arr).collect(groupingBy(YearMonth::getYear)).entrySet()
    				.stream().map(e-> new YearMonths(e.getKey(), e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)))
    				.toArray(YearMonths[]::new);
    	}
    }
}
