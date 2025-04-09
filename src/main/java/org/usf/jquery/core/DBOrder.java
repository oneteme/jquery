package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class DBOrder implements DBObject, Nested {

	private final DBColumn column;
	private final OrderType order;
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, DBOrder.class::getSimpleName);
		query.append(column);
		if(nonNull(order)) {
			query.appendSpace().append(order.name());
		}
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return column.compose(query, groupKeys);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
