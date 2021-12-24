package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonEmpty;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntervalCaseExpression extends CaseExpression {

	private final Number[] values;
	
	@Override
	protected String caseExpression(String columnName) {

		var sb = new StringBuilder(lessThanQuery(columnName, values[0]));
		for(var i=0; i<values.length-1; i++) {
			sb = sb.append(" "+betweenQuery(columnName, values[i], values[i+1]));
		}
		return sb.append(" "+greaterThanQuery(columnName, values[values.length-1])).toString();
	}
	
	public static IntervalCaseExpression intervals(int... serie) {
		
		return intervals(IntStream.of(requireNonEmpty(serie)).mapToObj(c-> c));
	}
	
	public static IntervalCaseExpression intervals(double... serie) {
		
		return intervals(DoubleStream.of(requireNonEmpty(serie)).mapToObj(c-> c));
	}

	private static IntervalCaseExpression intervals(Stream<Number> values) {
		
		return new IntervalCaseExpression(values.sorted().toArray(Number[]::new));
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
