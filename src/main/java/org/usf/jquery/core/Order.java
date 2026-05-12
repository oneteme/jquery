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
public final class Order implements DBObject {

	private final Column column;
	private final OrderType type;
	
	@Override
	public int prepare(QueryManifest declare) {
		return column.prepare(declare);
	}
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, Order.class::getSimpleName);
		query.append(column);
		if(nonNull(type)) {
			query.appendSpace().append(type.name());
		}
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
