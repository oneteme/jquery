package org.usf.jquery.core;

import static java.util.stream.Stream.concat;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class Partition implements DBObject, Groupable {

	private final DBColumn[] columns;
	private final  DBOrder[] orders;
	
	@Override
	public String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, Partition.class::getSimpleName);
		return sql(builder);
	}
	
	String sql(QueryVariables builder) {
		var sb = new SqlStringBuilder(100);
		if(!isEmpty(columns)) {
			sb.append("PARTITION BY ").append(builder.appendLiteralArray(columns));
		}
		if(!isEmpty(orders)) { //require orders
			sb.appendIf(!isEmpty(columns), SPACE)
			.append("ORDER BY ").append(builder.appendLiteralArray(orders));
		}
		return sb.toString();
	}

	@Override
	public Stream<DBColumn> groupKeys() {
		Stream<DBColumn> s = Stream.empty();
		if(!isEmpty(columns)) {
			s = Stream.of(columns);
		}
		if(!isEmpty(orders)) {
			s = concat(s, Stream.of(orders).map(DBOrder::getColumn));
		}
		return s;
	}
}
