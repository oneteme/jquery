package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static java.time.YearMonth.now;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.DBColumn.constant;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.YearViewDecorator.YearMonths.from;

import java.time.Year;
import java.time.YearMonth;
import java.util.stream.Stream;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
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
	static final String REVISION = "revision";
	
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

	default Builder<DBFilter> revisionFilter() { //optional filter ! monthRevision
    	return (view, env, args)-> {
    		var arr = isEmpty(args)
    				? stream(new YearMonth[] {now()}) 
    				: stream(args).map(YearViewDecorator::parseYearMonth);
    		env.currentQuery().repeat(from(arr));
    		return nonNull(monthRevision()) //optional month column
    				? view.column(monthRevision()).filter(in().expression((m,v)-> 
    					((YearMonths) requireNonNull(m, REVISION)).months()))
    				: null;
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
    	
    	static YearMonths[] from(Stream<YearMonth> stream) {
    		return stream.collect(groupingBy(YearMonth::getYear)).entrySet()
    				.stream().map(e-> new YearMonths(e.getKey(), e.getValue().stream().map(YearMonth::getMonthValue).toArray(Integer[]::new)))
    				.toArray(YearMonths[]::new);
    	}
    }
}
