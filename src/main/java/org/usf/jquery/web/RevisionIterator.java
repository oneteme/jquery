package org.usf.jquery.web;

import static java.util.stream.Collectors.groupingBy;
import static org.usf.jquery.core.Utils.isEmpty;

import java.time.YearMonth;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.usf.jquery.core.ColumnSingleFilter;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.QueryBuilder;
import org.usf.jquery.core.TableView;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class RevisionIterator implements Iterator<Entry<Integer, List<YearMonth>>> {

	private static final ThreadLocal<Entry<Integer, List<YearMonth>>> currentRev = new ThreadLocal<>();
	
	private final Iterator<Entry<Integer, List<YearMonth>>> it;
	
	@Override
	public boolean hasNext() {
		if(it.hasNext()) {
			return true;
		}
		currentRev.remove();
		return false;
	}
	
	@Override
	public Entry<Integer, List<YearMonth>> next() {
		var rev = it.next();
		currentRev.set(rev);
		return rev;
	}

	public static RevisionIterator iterator(YearMonth[] revisions){
		if(!isEmpty(revisions)) {
			var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
			return new RevisionIterator(map.entrySet().iterator());
		}
		throw new IllegalArgumentException("no revision");
	}
	
	public static Object[] iterator2(YearMonth[] revisions){
		if(!isEmpty(revisions)) {
			var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
			return map.entrySet().toArray();
		}
		throw new IllegalArgumentException("no revision");
	}

	static TableView yearTable(TableView view) {
		return null;
//		return new TableView(view.getSchema(), view.getName()) {
//			@Override
//			public void build(QueryBuilder query) {
//				super.build(query);
//				query.append("_" + currentRev.get().getKey());
//			}
//		};
	}

	static DBColumn yearColumn() {
		return null;//constant(INTEGER, ()-> currentRev.get().getKey()); //get it on build
	}
	
	static DBFilter monthFilter(DBColumn column) {
		return new ColumnSingleFilter(null, null) {
			@Override
			public void build(QueryBuilder query) {
				var values = currentRev.get().getValue();  //get it on build
				var filter = values.size() == 1 
						? column.eq(values.get(0).getMonthValue())
						: column.in(values.stream().map(YearMonth::getMonthValue).toArray(Integer[]::new));
				filter.build(query);
			}
		};
	}
}
