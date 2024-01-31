package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class OverClause implements DBObject {

	private final DBColumn[] partitions;
	private final DBOrder[] orders;
	
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
		if(!isEmpty(partitions)) {
			sb.append("PARTITION BY ").append(builder.appendLitteralArray(partitions));
		}
		if(!isEmpty(orders)) { //require orders
			sb.appendIf(nonNull(partitions), SPACE)
			.append("ORDER BY ").append(builder.appendLitteralArray(orders));
		}
		return sb.toString();
	}
}
