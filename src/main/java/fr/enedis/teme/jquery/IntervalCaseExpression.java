package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Objects.requireNonNull;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import lombok.Getter;

@Getter
public final class IntervalCaseExpression extends CaseExpressionColumn {

	private final Number[] values;
	
	private IntervalCaseExpression(DBColumn column, Number[] values) {
		super(column);
		this.values = values;
	}
	
	@Override
	protected String toSql(String columnName) {

		var sb = new StringBuilder(lessThanQuery(columnName, values[0]));
		for(int i=0; i<values.length-1; i++) {
			sb = sb.append(" "+betweenQuery(columnName, values[i], values[i+1]));
		}
		return sb.append(" "+greaterThanQuery(columnName, values[values.length-1])).toString();
	}
	
	public static IntervalCaseExpression intervals(DBColumn column, int... serie) {
		
		return new IntervalCaseExpression(
				requireNonNull(column), 
				IntStream.of(requireNonEmpty(serie)).sorted().mapToObj(c-> c).toArray(Integer[]::new));
	}
	
	public static IntervalCaseExpression intervals(DBColumn column, double... serie) {
		
		return new IntervalCaseExpression(
				requireNonNull(column), 
				DoubleStream.of(requireNonEmpty(serie)).sorted().mapToObj(c-> c).toArray(Double[]::new));
	}
	
	
	private static String lessThanQuery(String cn, Object val) {
		return whenThen(cn + "<" + val, "lt_" + val);
	}
	
	private static String greaterThanQuery(String cn, Object val) {
		return whenThen(cn + ">=" + val, "gt_" + val);
	}
	
	private static String betweenQuery(String cn, Object min, Object max) {
		return whenThen(cn + ">=" + min + " AND " + cn + "<" + max, "bt_" + min + "_" + max);
	}
	
}
