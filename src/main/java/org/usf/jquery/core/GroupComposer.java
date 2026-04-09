package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public class GroupComposer implements Composer<Group> {
	
	private List<Order> orders;
	
	public GroupComposer orders(@NonNull Order... orders) {
		if(isNull(this.orders)) {
			this.orders = new ArrayList<>();
		}
		addAll(this.orders, orders);
		return this;
	}
	
	@Override
	public Group compose() {
		return new Group(nonNull(orders) ? orders.toArray(Order[]::new) : null);
	}
}
