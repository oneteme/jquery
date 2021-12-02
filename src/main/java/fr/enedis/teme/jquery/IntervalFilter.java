package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.illegalArgumentIf;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

@Getter
public final class IntervalFilter<T> implements DBFilter {

	private final DBColumn column;
	private final T min; //int, long, double, date, timestamp
	private final boolean orMinEquals;
	private final T max;
	private final boolean orMaxEquals;

	public IntervalFilter(DBColumn column, T min, boolean orMinEquals, T max, boolean orMaxEquals) {
		this.column = requireNonNull(column);
		illegalArgumentIf(min == null && max == null, "min == max == null");
		this.min = min;
		this.orMinEquals = orMinEquals;
		this.max = max;
		this.orMaxEquals = orMaxEquals;
	}
	
	public IntervalFilter(DBColumn column, T min, T max) {
		this(column, min, false, max, false);
	}
	
	@Override
	public Collection<Object> args() {
		List<Object> list = new LinkedList<>();
		ofNullable(min).ifPresent(list::add);
		ofNullable(max).ifPresent(list::add);
		return list;
	}

	@Override
	public String toSql(DBTable table) {
		var cn = column.toSql(table);
		var c1 = strictOrEqual(cn, ">", orMinEquals, min);
		var c2 = strictOrEqual(cn, "<", orMaxEquals, max);
		if(c1 != null && c2 != null) {
			return c1 + " AND " + c2;
		}
		return c1 == null ? c2 : c1;
	}
	
	private static String strictOrEqual(String cn, String op, boolean orEquals, Object vl) {
		return ofNullable(vl)
				.map(s-> cn + op)
				.map(s-> orEquals ? s+"=" : s)
				.map(s-> s+"?")
				.orElse(null);
	}

	
	@Deprecated
	public IntervalFilter<String> asVarChar(){
		return new IntervalFilter<>(column, min == null ? null : min.toString(), orMinEquals, max == null ? null : max.toString(), orMaxEquals);
	}

}