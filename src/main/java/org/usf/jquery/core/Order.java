package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Order implements DBObject {

	private final DBColumn column;
	private final OrderType type;
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return column.compose(query, groupKeys);
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
