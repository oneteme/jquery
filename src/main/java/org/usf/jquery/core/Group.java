package org.usf.jquery.core;

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
	public int prepare(QueryManifest manifest) {
		return manifest.ignoreGroups(d-> d.prepareNested(orders));
	}

	@Override
	public void build(QueryBuilder query, Object... args) {
		query.append("ORDER BY ").appendEach(" ", orders);
	}
}
