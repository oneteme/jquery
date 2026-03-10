package org.usf.jquery.web.proxy;

import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Composer;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.Partition;

public final class PartitionComposer implements Composer<Partition> {
	
	private List<Column> columns;
	private List<Order> orders;

	public PartitionComposer columns(Column... columns) {
		if(isNull(columns)) {
			this.columns = new ArrayList<>();
		}
		addAll(this.columns, columns);
		return this;
	}
	
	public PartitionComposer orders(Order... orders) {
		if(isNull(orders)) {
			this.orders = new ArrayList<>();
		}
		addAll(this.orders, orders);
		return this;
	}
	
	@Override
	public Partition compose() {
		return new Partition(
				nonNull(columns) ? columns.toArray(Column[]::new) : null, 
				nonNull(orders) ? orders.toArray(Order[]::new) : null);
	}

}
