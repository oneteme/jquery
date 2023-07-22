package org.usf.jquery.core;

import static java.util.stream.Collectors.groupingBy;
import static org.usf.jquery.core.QueryParameterBuilder.parametrized;
import static org.usf.jquery.core.Utils.isEmpty;

import java.time.YearMonth;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
public final class PartitionedRequestQuery extends RequestQuery {
	
	private static final ThreadLocal<Entry<Integer, List<YearMonth>>> currentRev = new ThreadLocal<>();
	
	@Getter
	private final YearMonth[] revisions; // nullable
	
	public PartitionedRequestQuery(YearMonth... revisions) {
		this.revisions = revisions;
		if(isEmpty(revisions)) {
			this.noResult = true;
		}
	}
	
	@Override
	public final ParametredQuery build(){
		if(isEmpty(revisions)) {
			super.build(); //throw exception
		}
		var pb = parametrized();
		var sb = new SqlStringBuilder(500); // size ? 
		String[] cols = columns.stream().map(TaggableColumn::reference).toArray(String[]::new);
		var map = Stream.of(revisions).collect(groupingBy(YearMonth::getYear));
		sb.forEach(map.entrySet(), " UNION ALL ", e-> {
			currentRev.set(e);
			super.build(sb, pb);
		});
		currentRev.remove();
		return new ParametredQuery(sb.toString(), cols, pb.args(), noResult);
	}
	
	public static DBTable yearTable(String name) {
		return b-> name + "_" + currentRev.get().getKey();
	}

	public static DBColumn yearColumn() {
		return b-> currentRev.get().getKey().toString();
	}
	
	public static DBFilter monthFilter(DBColumn column) {
		return b-> {
			var values = currentRev.get().getValue();
			var filter = values.size() == 1 
					? column.equal(values.get(0).getMonthValue())
					: column.in(values.stream().map(YearMonth::getMonthValue).toArray(Integer[]::new));
			return filter.sql(b);
		};
	}
}
