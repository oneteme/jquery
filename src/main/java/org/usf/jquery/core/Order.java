package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireNoArgs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Order implements QueryPart {

	private final Column column;
	private final OrderType type;
	
	@Override
	public int prepare(QueryAnalyzer analyzer) {
		return column.prepare(analyzer);
	}
	
	@Override
	public void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, Order.class::getSimpleName);
		builder.append(column);
		if(nonNull(type)) {
			builder.appendSpace().append(type.name());
		}
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
