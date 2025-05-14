package org.usf.jquery.web;

import static java.time.Month.DECEMBER;
import static java.time.YearMonth.now;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.DBColumn.constant;
import static org.usf.jquery.core.JDBCType.INTEGER;
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
	
	static ColumnBuilder yearRevision() {
		return view-> {
			if(view instanceof YearViewDecorator) {
				return constant(INTEGER, (m, v)-> ((YearMonths)m).year());
			}
			throw new IllegalStateException(view.getClass().getSimpleName() + " is not a YearView");
		};
	}
	
	@Override
	default DBView view() {
		var view = ViewDecorator.super.builder().build();
		if(view instanceof TableView t) {
			return t.withAdjuster((m, v)-> v + "_" + ((YearMonths)m).year());
		}
		throw new UnsupportedOperationException(requireNonNull(view).getClass().getSimpleName());
	}
	
	@Override
	default CriteriaBuilder<DBFilter> criteria(String name) {
		if(REVISION.equals(name)) {
			return revisionFilter(this.column(monthRevision()));
		}
		return ViewDecorator.super.criteria(name);
	}

	static CriteriaBuilder<DBFilter> revisionFilter(DBColumn monthColumn){
    	return (ctx, args)-> {
    		if(isNull(args) || args.length == 0) {
    			args = new String[] {now().toString()};  //TODO funct. validation
    		}
    		ctx.getQuery().repeat(from(stream(args).map(YearViewDecorator::parseYearMonth)));
    		return monthColumn.filter(in().expression((m,v)-> ((YearMonths) requireNonNull(m, REVISION)).months()));
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
