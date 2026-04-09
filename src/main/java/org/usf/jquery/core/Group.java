package org.usf.jquery.core;

import static org.usf.jquery.core.QueryDeclaration.DECLARE_VIEW_ONLY;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class Group implements DBObject {
	
	private final Order[] orders;

	@Override
	public int compose(QueryDeclaration composer) {
		return composer.sub(DECLARE_VIEW_ONLY).composeNested(orders); //check this
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		query.append("ORDER BY ").appendEach(" ", orders);
	}
}
