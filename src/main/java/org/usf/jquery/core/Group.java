package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;

/**
 * 
 * @author u$f
 *
 */
public final class Group implements DBObject {
	
	private final Collection<Order> orders;

	public Group(Collection<Order> orders) {
		if(isEmpty(orders)) {
			throw new ComposeException("within group requires at least one order");
		}
		this.orders = orders;
	}

	@Override
	public int prepare(QueryManifest manifest) {
		return manifest.ignoreGroups(d-> d.prepareNested(orders));
	}

	@Override
	public void build(QueryBuilder builder, Object... args) {
		builder.append("ORDER BY ").appendEach(SPACE, orders);
	}
}
