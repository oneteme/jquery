package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableCollection;

import java.util.ArrayList;
import java.util.Collection;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public final class GroupComposer implements Composer<Group> {
	
	private Collection<Order> orders = new ArrayList<>();
	
	public GroupComposer order(@NonNull Order order) {
		getOrders().add(order);
		return this;
	}
	
	public GroupComposer orders(@NonNull Order... orders) {
		addAll(getOrders(), orders);
		return this;
	}
	
	public GroupComposer orders(@NonNull Collection<Order> orders) {
		getOrders().addAll(orders);
		return this;
	}
	
	private Collection<Order> getOrders(){
		return orders;
	}
	
	@Override
	public Group compose(Store store) {
		return new Group(unmodifiableCollection(orders));
	}
}
