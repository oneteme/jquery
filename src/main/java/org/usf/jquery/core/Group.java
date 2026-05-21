package org.usf.jquery.core;

import static org.usf.jquery.core.QueryAnalyzer.IGNORE_GROUPS;
import static org.usf.jquery.core.SqlBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collection;

/**
 * 
 * @author u$f
 *
 */
public final class Group implements QueryPart {
	
	private final Collection<Order> orders;

	public Group(Collection<Order> orders) {
		if(isEmpty(orders)) {
			throw new ComposeException("within group requires at least one order");
		}
		this.orders = orders;
	}

	@Override
	public int prepare(QueryAnalyzer manifest) {
		return manifest.with(IGNORE_GROUPS).analyzeNested(orders);
	}

	@Override
	public void build(SqlBuilder builder, Object... args) {
		builder.append("ORDER BY ").appendEach(SPACE, orders);
	}
}
