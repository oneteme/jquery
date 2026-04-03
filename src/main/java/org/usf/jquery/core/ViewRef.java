package org.usf.jquery.core;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ViewRef implements DBView {
	
	private final DBView view;
	
	@Override
	public int compose(QueryDeclaration composer) {
		throw new IllegalStateException("ViewRef cannot be composed");
	}

	@Override
	public void build(QueryBuilder query) {
		query.appendViewAlias(view); // append view as cte reference
	}
	
	@Override
	public DBView resolveView(QueryBuilder query) {
		return this;
	}

	@Override
	public ViewRef asReference() {
		return this;
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
