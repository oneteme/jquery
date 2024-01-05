package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OverClause implements DBObject {

	private final OperationColumn partition;
	private final OperationColumn order;
	
	public OverClause() {
		this(null, null);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, OverClause.class::getSimpleName);
		return sql(builder);
	}
	
	String sql(QueryParameterBuilder builder) {
		var sb = new SqlStringBuilder(100);
		if(nonNull(partition)) {
			sb.append(partition.sql(builder));
		}
		if(nonNull(order)) { //require orders
			sb.appendIf(nonNull(partition), SPACE).append(order.sql(builder));
		}
		return sb.toString();
	}
	
	public static OverClause clauses(OperationColumn... args) { //partition, order, ...
		if(args == null) {
			return new OverClause();
		}
		var map = Stream.of(args).collect(groupingBy(o-> o.getOperator().id()));
		var prt = requireOneArg(map, "PARTITION BY"); 
		var ord = requireOneArg(map, "ORDER BY");
		if(map.isEmpty()) {
			return new OverClause(prt, ord);
		}
		throw new IllegalArgumentException("illegal over function arguments : " + map.keySet());
	}

	private static OperationColumn requireOneArg(Map<String, List<OperationColumn>> map, String key) {
		var args = map.remove(key);
		if(isNull(args)) {
			return null;
		}
		if(args.size() == 1) {
			//instance of ClauseFunction ?
			return args.get(0);
		}
		throw new IllegalArgumentException("duplicated arg values " + key);
	}
}
